package com.digitalpetri.enip.cip;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.digitalpetri.enip.EtherNetIpClient;
import com.digitalpetri.enip.EtherNetIpClientConfig;
import com.digitalpetri.enip.cip.epath.EPath;
import com.digitalpetri.enip.cip.services.CipService;
import com.digitalpetri.enip.cip.services.CipServiceInvoker;
import com.digitalpetri.enip.cip.services.UnconnectedSendService;
import com.digitalpetri.enip.commands.SendRRData;
import com.digitalpetri.enip.commands.SendUnitData;
import com.digitalpetri.enip.cpf.ConnectedAddressItem;
import com.digitalpetri.enip.cpf.ConnectedDataItemRequest;
import com.digitalpetri.enip.cpf.ConnectedDataItemResponse;
import com.digitalpetri.enip.cpf.CpfItem;
import com.digitalpetri.enip.cpf.CpfPacket;
import com.digitalpetri.enip.cpf.NullAddressItem;
import com.digitalpetri.enip.cpf.UnconnectedDataItemRequest;
import com.digitalpetri.enip.cpf.UnconnectedDataItemResponse;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class CipClient extends EtherNetIpClient implements CipServiceInvoker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConnectedDataHandler connectedDataHandler = new ConnectedDataHandler();
    private final List<CpfItemHandler> additionalHandlers = new CopyOnWriteArrayList<>();

    private final Map<Integer, CompletableFuture<ByteBuf>> pending = new ConcurrentHashMap<>();
    private final Map<Integer, Timeout> timeouts = new ConcurrentHashMap<>();

    private final AtomicInteger sequenceNumber = new AtomicInteger(0);

    private final EPath.PaddedEPath connectionPath;

    public CipClient(EtherNetIpClientConfig config, EPath.PaddedEPath connectionPath) {
        super(config);

        this.connectionPath = connectionPath;
    }

    @Override
    public <T> CompletableFuture<T> invokeConnected(int connectionId, CipService<T> service) {
        CompletableFuture<T> future = new CompletableFuture<>();

        return invokeConnected(connectionId, service, future);
    }

    private <T> CompletableFuture<T> invokeConnected(int connectionId,
                                                     CipService<T> service,
                                                     CompletableFuture<T> future) {

        sendConnectedData(service::encodeRequest, connectionId).whenComplete((buffer, ex) -> {
            if (buffer != null) {
                try {
                    T response = service.decodeResponse(buffer);

                    future.complete(response);
                } catch (CipService.PartialResponseException e) {
                    invokeConnected(connectionId, service, future);
                } catch (CipResponseException e) {
                    future.completeExceptionally(e);
                } finally {
                    ReferenceCountUtil.release(buffer);
                }
            } else {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    @Override
    public <T> CompletableFuture<T> invokeUnconnected(CipService<T> service) {
        return invokeUnconnected(service, 0);
    }

    @Override
    public <T> CompletableFuture<T> invokeUnconnected(CipService<T> service, int maxRetries) {
        UnconnectedSendService<T> uss = new UnconnectedSendService<T>(
            service,
            connectionPath,
            getConfig().getTimeout()
        );

        return invoke(uss, maxRetries);
    }

    @Override
    public <T> CompletableFuture<T> invoke(CipService<T> service) {
        return invoke(service, 0);
    }

    @Override
    public <T> CompletableFuture<T> invoke(CipService<T> service, int maxRetries) {
        CompletableFuture<T> future = new CompletableFuture<>();

        return invoke(service, future, 0, maxRetries);
    }

    private <T> CompletableFuture<T> invoke(CipService<T> service,
                                            CompletableFuture<T> future,
                                            int count, int maxRetries) {

        sendUnconnectedData(service::encodeRequest).whenComplete((buffer, ex) -> {
            if (buffer != null) {
                try {
                    T response = service.decodeResponse(buffer);

                    future.complete(response);
                } catch (CipService.PartialResponseException e) {
                    invoke(service, future, count, maxRetries);
                } catch (CipResponseException e) {
                    if (e.getGeneralStatus() == 0x01) {
                        boolean requestTimedOut = Arrays.stream(e.getAdditionalStatus()).anyMatch(i -> i == 0x0204);

                        if (requestTimedOut && count < maxRetries) {
                            getConfig().getLoggingContext().forEach(MDC::put);
                            try {
                                logger.debug("Unconnected request timed out; " +
                                    "retrying, count={}, max={}", count, maxRetries);
                            } finally {
                                getConfig().getLoggingContext().keySet().forEach(MDC::remove);
                            }

                            invoke(service, future, count + 1, maxRetries);
                        } else {
                            future.completeExceptionally(e);
                        }
                    } else {
                        future.completeExceptionally(e);
                    }
                } finally {
                    ReferenceCountUtil.release(buffer);
                }
            } else {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    public CompletableFuture<ByteBuf> sendConnectedData(ByteBuf data, int connectionId) {
        return sendConnectedData((buffer) -> buffer.writeBytes(data), connectionId);
    }

    public CompletableFuture<ByteBuf> sendConnectedData(Consumer<ByteBuf> dataEncoder, int connectionId) {
        CompletableFuture<ByteBuf> future = new CompletableFuture<>();

        ConnectedAddressItem addressItem = new ConnectedAddressItem(connectionId);

        int sequenceNumber = nextSequenceNumber();

        ConnectedDataItemRequest dataItem = new ConnectedDataItemRequest((b) -> {
            b.writeShort(sequenceNumber);
            dataEncoder.accept(b);
        });

        CpfPacket packet = new CpfPacket(addressItem, dataItem);
        SendUnitData command = new SendUnitData(packet);

        Timeout timeout = getConfig().getWheelTimer().newTimeout(tt -> {
            if (tt.isCancelled()) return;
            CompletableFuture<ByteBuf> f = pending.remove(sequenceNumber);
            if (f != null) {
                String message = String.format(
                    "sequenceNumber=%s timed out waiting %sms for response",
                    sequenceNumber, getConfig().getTimeout().toMillis()
                );
                f.completeExceptionally(new TimeoutException(message));
            }
        }, getConfig().getTimeout().toMillis(), TimeUnit.MILLISECONDS);

        pending.put(sequenceNumber, future);
        timeouts.put(sequenceNumber, timeout);

        sendUnitData(command).whenComplete((v, ex) -> {
            // sendUnitData() fails fast if the channel isn't available
            if (ex != null) future.completeExceptionally(ex);
        });

        return future;
    }

    public CompletableFuture<ByteBuf> sendUnconnectedData(ByteBuf data) {
        return sendUnconnectedData((buffer) -> buffer.writeBytes(data));
    }

    public CompletableFuture<ByteBuf> sendUnconnectedData(Consumer<ByteBuf> dataEncoder) {
        CompletableFuture<ByteBuf> future = new CompletableFuture<>();

        UnconnectedDataItemRequest dataItem = new UnconnectedDataItemRequest(dataEncoder);
        CpfPacket packet = new CpfPacket(new NullAddressItem(), dataItem);

        sendRRData(new SendRRData(packet)).whenComplete((command, ex) -> {
            if (command != null) {
                CpfItem[] items = command.getPacket().getItems();

                if (items.length == 2 &&
                    items[0].getTypeId() == NullAddressItem.TYPE_ID &&
                    items[1].getTypeId() == UnconnectedDataItemResponse.TYPE_ID) {

                    ByteBuf data = ((UnconnectedDataItemResponse) items[1]).getData();

                    future.complete(data);
                } else {
                    future.completeExceptionally(new Exception("received unexpected items"));
                }
            } else {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    @Override
    protected void onUnitDataReceived(SendUnitData command) {
        CpfItem[] items = command.getPacket().getItems();

        if (connectedDataHandler.itemsMatch(items)) {
            connectedDataHandler.itemsReceived(items);
        } else {
            for (CpfItemHandler handler : additionalHandlers) {
                if (handler.itemsMatch(items)) {
                    handler.itemsReceived(items);
                    break;
                }
            }
        }
    }

    private short nextSequenceNumber() {
        return (short) sequenceNumber.incrementAndGet();
    }

    private class ConnectedDataHandler implements CpfItemHandler {

        @Override
        public void itemsReceived(CpfItem[] items) {
            int connectionId = ((ConnectedAddressItem) items[0]).getConnectionId();
            ByteBuf buffer = ((ConnectedDataItemResponse) items[1]).getData();

            int sequenceNumber = buffer.readShort();
            ByteBuf data = buffer.readSlice(buffer.readableBytes()).retain();

            Timeout timeout = timeouts.remove(sequenceNumber);
            if (timeout != null) timeout.cancel();

            CompletableFuture<ByteBuf> future = pending.remove(sequenceNumber);

            if (future != null) {
                future.complete(data);
            } else {
                ReferenceCountUtil.release(data);
            }

            ReferenceCountUtil.release(buffer);
        }

        @Override
        public boolean itemsMatch(CpfItem[] items) {
            return items.length == 2 &&
                items[0].getTypeId() == ConnectedAddressItem.TYPE_ID &&
                items[1].getTypeId() == ConnectedDataItemResponse.TYPE_ID;
        }

    }

    public static interface CpfItemHandler {
        boolean itemsMatch(CpfItem[] items);

        void itemsReceived(CpfItem[] items);
    }

}
