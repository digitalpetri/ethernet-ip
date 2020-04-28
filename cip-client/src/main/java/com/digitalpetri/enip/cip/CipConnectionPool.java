/*
 * Copyright 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalpetri.enip.cip;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.epath.LogicalSegment.ClassId;
import com.digitalpetri.enip.cip.epath.LogicalSegment.InstanceId;
import com.digitalpetri.enip.cip.services.CipService.PartialResponseException;
import com.digitalpetri.enip.cip.services.ForwardCloseService;
import com.digitalpetri.enip.cip.services.ForwardOpenService;
import com.digitalpetri.enip.cip.services.LargeForwardOpenService;
import com.digitalpetri.enip.cip.structs.ForwardCloseRequest;
import com.digitalpetri.enip.cip.structs.ForwardCloseResponse;
import com.digitalpetri.enip.cip.structs.ForwardOpenRequest;
import com.digitalpetri.enip.cip.structs.ForwardOpenResponse;
import com.digitalpetri.enip.cip.structs.LargeForwardOpenRequest;
import com.digitalpetri.enip.cip.structs.LargeForwardOpenResponse;
import com.digitalpetri.enip.cip.structs.NetworkConnectionParameters;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


public class CipConnectionPool {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, String> loggingContext;

    private final Queue<CipConnection> queue = new LinkedList<>();

    private final Queue<CompletableFuture<CipConnection>> waitQueue = new LinkedList<>();
    private final AtomicInteger count = new AtomicInteger(0);

    private final int connectionLimit;
    private final CipConnectionFactory connectionFactory;

    public CipConnectionPool(int connectionLimit, CipClient client, PaddedEPath connectionPath, int connectionSize) {
        this(
            connectionLimit,
            new DefaultConnectionFactory(client, connectionPath, connectionSize),
            client.getConfig().getLoggingContext()
        );
    }

    public CipConnectionPool(
        int connectionLimit,
        CipConnectionFactory connectionFactory,
        Map<String, String> loggingContext
    ) {

        this.connectionLimit = connectionLimit;
        this.connectionFactory = connectionFactory;
        this.loggingContext = loggingContext;
    }

    public synchronized CompletableFuture<CipConnection> acquire() {
        CompletableFuture<CipConnection> future = new CompletableFuture<>();

        acquire0().whenComplete((c, ex) -> {
            if (c != null) {
                if (c.isExpired()) {
                    remove(c);

                    acquire().whenComplete((c2, ex2) -> {
                        if (c2 != null) future.complete(c2);
                        else future.completeExceptionally(ex2);
                    });
                } else {
                    future.complete(c);
                }
            } else {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    private synchronized CompletableFuture<CipConnection> acquire0() {
        CompletableFuture<CipConnection> future = new CompletableFuture<>();

        if (!queue.isEmpty()) {
            future.complete(queue.poll());
        } else {
            waitQueue.add(future);

            if (count.incrementAndGet() <= connectionLimit) {
                CompletableFuture<CipConnection> f = connectionFactory.open();

                f.whenComplete((c, ex) -> {
                    CompletableFuture<CipConnection> waiter;
                    synchronized (CipConnectionPool.this) {
                        waiter = waitQueue.poll();
                    }

                    if (c != null) {
                        if (waiter != null) {
                            waiter.complete(c);
                        } else {
                            queue.add(c);
                        }
                        loggingContext.forEach(MDC::put);
                        try {
                            logger.debug("Forward open succeeded: {}", c);
                        } finally {
                            loggingContext.keySet().forEach(MDC::remove);
                        }
                    } else {
                        count.decrementAndGet();
                        if (waiter != null) waiter.completeExceptionally(ex);

                        loggingContext.forEach(MDC::put);
                        try {
                            logger.debug("Forward open failed: {}", ex.getMessage(), ex);
                        } finally {
                            loggingContext.keySet().forEach(MDC::remove);
                        }
                    }
                });
            } else {
                count.decrementAndGet();
            }
        }

        return future;
    }

    public synchronized void release(CipConnection connection) {
        connection.updateLastUse();

        if (!waitQueue.isEmpty()) {
            waitQueue.poll().complete(connection);
        } else {
            queue.add(connection);
        }
    }

    public synchronized void remove(CipConnection connection) {
        connectionFactory.close(connection).thenRun(
            () -> {
                loggingContext.forEach(MDC::put);
                try {
                    logger.debug("Connection closed: {}", connection);
                } finally {
                    loggingContext.keySet().forEach(MDC::remove);
                }
            }
        );

        queue.remove(connection);
        count.decrementAndGet();

        if (!waitQueue.isEmpty()) {
            CompletableFuture<CipConnection> next = waitQueue.poll();

            acquire().whenComplete((c, ex) -> {
                if (c != null) next.complete(c);
                else next.completeExceptionally(ex);
            });
        }
    }

    public interface CipConnectionFactory {

        CompletableFuture<CipConnection> open();

        CompletableFuture<ForwardCloseResponse> close(CipConnection connection);

    }

    public static class DefaultConnectionFactory implements CipConnectionFactory {

        private static final Duration DEFAULT_RPI = Duration.ofSeconds(2);
        private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

        private static final PaddedEPath MESSAGE_ROUTER_CP_PATH = new PaddedEPath(
            new ClassId(0x02),
            new InstanceId(0x01)
        );

        private static final AtomicInteger T2O_CONNECTION_ID = new AtomicInteger(0);

        private final CipClient client;
        private final PaddedEPath connectionPath;
        private final int connectionSize;

        public DefaultConnectionFactory(CipClient client, PaddedEPath connectionPath, int connectionSize) {
            this.client = client;
            this.connectionPath = connectionPath;
            this.connectionSize = connectionSize;
        }

        @Override
        public CompletableFuture<CipConnection> open() {
            return connectionSize <= 500 ?
                forwardOpen() : largeForwardOpen();
        }

        private CompletableFuture<CipConnection> forwardOpen() {
            CompletableFuture<CipConnection> future = new CompletableFuture<>();

            NetworkConnectionParameters parameters = getNetworkConnectionParameters();

            ForwardOpenRequest request = new ForwardOpenRequest(
                DEFAULT_TIMEOUT,
                0,
                T2O_CONNECTION_ID.incrementAndGet(),
                new Random().nextInt(),
                client.getConfig().getVendorId(),
                client.getConfig().getSerialNumber(),
                1, // 0 = x4, 1 = x8, 2 = x16, 3 = x32, 4 = x128, 5 = x256, 6 = x512
                connectionPath.append(MESSAGE_ROUTER_CP_PATH),
                DEFAULT_RPI,
                parameters,
                DEFAULT_RPI,
                parameters,
                0xA3);

            ForwardOpenService service = new ForwardOpenService(request);

            client.sendUnconnectedData(service::encodeRequest).whenComplete((b, ex) -> {
                if (b != null) {
                    try {
                        ForwardOpenResponse response = service.decodeResponse(b);

                        CipConnection connection = new CipConnection(
                            DEFAULT_TIMEOUT.toNanos(),
                            response.getO2tConnectionId(),
                            response.getT2oConnectionId(),
                            response.getConnectionSerialNumber(),
                            response.getOriginatorVendorId(),
                            response.getOriginatorSerialNumber());

                        ReferenceCountUtil.release(response.getApplicationReply());

                        future.complete(connection);
                    } catch (CipResponseException | PartialResponseException e) {
                        future.completeExceptionally(e);
                    } finally {
                        ReferenceCountUtil.release(b);
                    }
                } else {
                    future.completeExceptionally(ex);
                }
            });

            return future;
        }

        protected NetworkConnectionParameters getNetworkConnectionParameters() {
            return new NetworkConnectionParameters(
                connectionSize,
                NetworkConnectionParameters.SizeType.Variable,
                NetworkConnectionParameters.Priority.Low,
                NetworkConnectionParameters.ConnectionType.PointToPoint,
                false);
        }

        private CompletableFuture<CipConnection> largeForwardOpen() {
            CompletableFuture<CipConnection> future = new CompletableFuture<>();

            NetworkConnectionParameters parameters = getNetworkConnectionParameters();

            LargeForwardOpenRequest request = new LargeForwardOpenRequest(
                DEFAULT_TIMEOUT,                                // timeout
                0,                                              // o2tConnectionId
                T2O_CONNECTION_ID.incrementAndGet(),            // t2oConnectionId
                new Random().nextInt(),                         // connectionSerialNumber
                client.getConfig().getVendorId(),               // vendorId
                client.getConfig().getSerialNumber(),           // vendorSerialNumber
                1,                                              // connectionTimeoutMultiplier
                connectionPath.append(MESSAGE_ROUTER_CP_PATH),  // connectionPath
                DEFAULT_RPI,                                    // o2tRpi
                parameters,                                     // o2tParameters
                DEFAULT_RPI,                                    // t2oRpi
                parameters,                                     // t2oParameters
                0xA3);                                          // transportClassAndTrigger

            LargeForwardOpenService service = new LargeForwardOpenService(request);

            client.sendUnconnectedData(service::encodeRequest).whenComplete((b, ex) -> {
                if (b != null) {
                    try {
                        LargeForwardOpenResponse response = service.decodeResponse(b);

                        CipConnection connection = new CipConnection(
                            DEFAULT_TIMEOUT.toNanos(),
                            response.getO2tConnectionId(),
                            response.getT2oConnectionId(),
                            response.getConnectionSerialNumber(),
                            response.getOriginatorVendorId(),
                            response.getOriginatorSerialNumber());

                        ReferenceCountUtil.release(response.getApplicationReply());

                        future.complete(connection);
                    } catch (CipResponseException | PartialResponseException e) {
                        future.completeExceptionally(e);
                    } finally {
                        ReferenceCountUtil.release(b);
                    }
                } else {
                    future.completeExceptionally(ex);
                }
            });

            return future;
        }

        @Override
        public CompletableFuture<ForwardCloseResponse> close(CipConnection connection) {
            CompletableFuture<ForwardCloseResponse> future = new CompletableFuture<>();

            ForwardCloseRequest request = new ForwardCloseRequest(
                Duration.ofNanos(connection.getTimeoutNanos()),
                connection.getSerialNumber(),
                connection.getOriginatorVendorId(),
                connection.getOriginatorSerialNumber(),
                connectionPath.append(MESSAGE_ROUTER_CP_PATH)
            );

            ForwardCloseService service = new ForwardCloseService(request);

            client.sendUnconnectedData(service::encodeRequest).whenComplete((b, ex) -> {
                if (b != null) {
                    try {
                        ForwardCloseResponse response = service.decodeResponse(b);

                        future.complete(response);
                    } catch (CipResponseException | PartialResponseException e) {
                        future.completeExceptionally(e);
                    } finally {
                        ReferenceCountUtil.release(b);
                    }
                } else {
                    future.completeExceptionally(ex);
                }
            });

            return future;
        }

    }

    public static class CipConnection {

        private volatile long lastUse = System.nanoTime();

        private final long timeoutNanos;
        private final int o2tConnectionId;
        private final int t2oConnectionId;
        private final int serialNumber;
        private final int originatorVendorId;
        private final long originatorSerialNumber;

        public CipConnection(long timeoutNanos,
                             int o2tConnectionId,
                             int t2oConnectionId,
                             int serialNumber,
                             int originatorVendorId,
                             long originatorSerialNumber) {

            this.timeoutNanos = timeoutNanos;
            this.o2tConnectionId = o2tConnectionId;
            this.t2oConnectionId = t2oConnectionId;
            this.serialNumber = serialNumber;
            this.originatorVendorId = originatorVendorId;
            this.originatorSerialNumber = originatorSerialNumber;
        }

        public long getTimeoutNanos() {
            return timeoutNanos;
        }

        public int getO2tConnectionId() {
            return o2tConnectionId;
        }

        public int getT2oConnectionId() {
            return t2oConnectionId;
        }

        public int getSerialNumber() {
            return serialNumber;
        }

        public int getOriginatorVendorId() {
            return originatorVendorId;
        }

        public long getOriginatorSerialNumber() {
            return originatorSerialNumber;
        }

        void updateLastUse() {
            lastUse = System.nanoTime();
        }

        boolean isExpired() {
            return (System.nanoTime() - lastUse) > timeoutNanos;
        }

        @Override
        public String toString() {
            return "CipConnection{" +
                "o2tConnectionId=" + o2tConnectionId +
                ", t2oConnectionId=" + t2oConnectionId +
                ", serialNumber=" + serialNumber +
                '}';
        }

    }

}
