package com.digitalpetri.enip;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.util.HashedWheelTimer;

public class EtherNetIpClientConfig {

    private final String hostname;
    private final int port;
    private final int vendorId;
    private final int serialNumber;
    private final Duration timeout;
    private final Duration maxIdle;
    private final int maxReconnectDelaySeconds;
    private final boolean lazy;
    private final boolean persistent;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutor;
    private final EventLoopGroup eventLoop;
    private final HashedWheelTimer wheelTimer;
    private final Consumer<Bootstrap> bootstrapConsumer;
    private final Map<String, String> loggingContext;

    public EtherNetIpClientConfig(
        String hostname,
        int port,
        int vendorId,
        int serialNumber,
        Duration timeout,
        Duration maxIdle,
        int maxReconnectDelaySeconds,
        boolean lazy,
        boolean persistent,
        ExecutorService executor,
        ScheduledExecutorService scheduledExecutor,
        EventLoopGroup eventLoop,
        HashedWheelTimer wheelTimer,
        Consumer<Bootstrap> bootstrapConsumer,
        Map<String, String> loggingContext
    ) {

        this.hostname = hostname;
        this.port = port;
        this.vendorId = vendorId;
        this.serialNumber = serialNumber;
        this.timeout = timeout;
        this.maxIdle = maxIdle;
        this.maxReconnectDelaySeconds = maxReconnectDelaySeconds;
        this.lazy = lazy;
        this.persistent = persistent;
        this.executor = executor;
        this.scheduledExecutor = scheduledExecutor;
        this.eventLoop = eventLoop;
        this.wheelTimer = wheelTimer;
        this.bootstrapConsumer = bootstrapConsumer;
        this.loggingContext = loggingContext;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public int getVendorId() {
        return vendorId;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public Duration getTimeout() {
        return timeout;
    }

    /**
     * @return the max amount of time that can elapse without reading any data from the remote before a keep alive
     * ListIdentity request is sent. If this ListIdentity request fails for any reason the channel is closed.
     */
    public Duration getMaxIdle() {
        return maxIdle;
    }

    /**
     * Get the maximum amount of time to delay between reconnect attempts, in seconds.
     * <p>
     * Delays between reconnect attempts are exponentially backed off starting from 1 until {@code maxReconnectDelay}
     * is reached.
     * <p>
     * Must be a power of 2 or else it will be rounded up to the nearest.
     *
     * @return the maximum amount of time to delay, in seconds, between reconnect attempts.
     */
    public int getMaxReconnectDelaySeconds() {
        return maxReconnectDelaySeconds;
    }

    /**
     * @return {@code true} if the channel state machine is lazy in its reconnection attempts, i.e. after a break in the
     * connection occurs it moves to the Idle state, reconnecting on demand the next time the connect() or getChannel()
     * is called. If {@code false} the state machine eagerly attempts to reconnect and move back into Connected state.
     */
    public boolean isLazy() {
        return lazy;
    }

    /**
     * @return {@code true} if the channel state machine is persistent in its connection attempts, i.e. after a
     * single call to connect() it strives to stay in a Connected state (respecting laziness) regardless of the result
     * of the initial connect(). If {@code false}, the state machine won't attempt to remain connected until it has
     * successfully moved into the Connected state.
     */
    public boolean isPersistent() {
        return persistent;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public EventLoopGroup getEventLoop() {
        return eventLoop;
    }

    public HashedWheelTimer getWheelTimer() {
        return wheelTimer;
    }

    public Consumer<Bootstrap> getBootstrapConsumer() {
        return bootstrapConsumer;
    }

    /**
     * Get the logging context Map.
     * <p>
     * Keys and values in the Map will be set on the SLF4J {@link org.slf4j.MDC} when logging.
     *
     * @return the logging context Map an {@link EtherNetIpClient} instance will use when logging.
     */
    public Map<String, String> getLoggingContext() {
        return loggingContext;
    }

    public static Builder builder(String hostname) {
        return new Builder().setHostname(hostname);
    }

    public static class Builder {

        private String hostname;
        private int port = 44818;
        private int vendorId = 0;
        private int serialNumber = 0;
        private Duration timeout = Duration.ofSeconds(5);
        private Duration maxIdle = Duration.ofSeconds(15);
        private int maxReconnectDelaySeconds = 16;
        private boolean lazy = true;
        private boolean persistent = true;
        private ExecutorService executor;
        private ScheduledExecutorService scheduledExecutor;
        private EventLoopGroup eventLoop;
        private HashedWheelTimer wheelTimer;
        private Consumer<Bootstrap> bootstrapConsumer = (b) -> {};
        private Map<String, String> loggingContext = new ConcurrentHashMap<>();

        public Builder setHostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setVendorId(int vendorId) {
            this.vendorId = vendorId;
            return this;
        }

        public Builder setSerialNumber(int serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        public Builder setTimeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * @see EtherNetIpClientConfig#getMaxIdle()
         */
        public Builder setMaxIdle(Duration maxIdle) {
            this.maxIdle = maxIdle;
            return this;
        }

        /**
         * @see EtherNetIpClientConfig#getMaxReconnectDelaySeconds()
         */
        public Builder setMaxReconnectDelaySeconds(int maxReconnectDelaySeconds) {
            this.maxReconnectDelaySeconds = maxReconnectDelaySeconds;
            return this;
        }

        /**
         * @see EtherNetIpClientConfig#isLazy()
         */
        public Builder setLazy(boolean lazy) {
            this.lazy = lazy;
            return this;
        }

        /**
         * @see EtherNetIpClientConfig#isPersistent()
         */
        public Builder setPersistent(boolean persistent) {
            this.persistent = persistent;
            return this;
        }

        public Builder setExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public Builder setScheduledExecutor(ScheduledExecutorService scheduledExecutor) {
            this.scheduledExecutor = scheduledExecutor;
            return this;
        }

        public Builder setEventLoop(EventLoopGroup eventLoop) {
            this.eventLoop = eventLoop;
            return this;
        }

        public Builder setWheelTimer(HashedWheelTimer wheelTimer) {
            this.wheelTimer = wheelTimer;
            return this;
        }

        public Builder setBootstrapConsumer(Consumer<Bootstrap> bootstrapConsumer) {
            this.bootstrapConsumer = bootstrapConsumer;
            return this;
        }

        /**
         * Set the logging context Map an {@link EtherNetIpClient} instance will use.
         * <p>
         * Keys and values in the Map will be set on the SLF4J {@link org.slf4j.MDC} when logging.
         * <p>
         * This method makes a defensive copy of {@code loggingContext}.
         *
         * @param loggingContext the logging context for this {@link EtherNetIpClient} instance.
         * @return this {@link Builder} instance.
         */
        public Builder setLoggingContext(Map<String, String> loggingContext) {
            this.loggingContext = new ConcurrentHashMap<>(loggingContext);
            return this;
        }

        public EtherNetIpClientConfig build() {
            if (executor == null) {
                executor = EtherNetIpShared.sharedExecutorService();
            }
            if (scheduledExecutor == null) {
                scheduledExecutor = EtherNetIpShared.sharedScheduledExecutor();
            }
            if (eventLoop == null) {
                eventLoop = EtherNetIpShared.sharedEventLoop();
            }
            if (wheelTimer == null) {
                wheelTimer = EtherNetIpShared.sharedWheelTimer();
            }

            return new EtherNetIpClientConfig(
                hostname,
                port,
                vendorId,
                serialNumber,
                timeout,
                maxIdle,
                maxReconnectDelaySeconds,
                lazy,
                persistent,
                executor,
                scheduledExecutor,
                eventLoop,
                wheelTimer,
                bootstrapConsumer,
                loggingContext
            );
        }
    }

}
