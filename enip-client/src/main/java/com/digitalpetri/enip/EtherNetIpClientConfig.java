package com.digitalpetri.enip;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

import io.netty.channel.EventLoopGroup;
import io.netty.util.HashedWheelTimer;

public class EtherNetIpClientConfig {

    private final String hostname;
    private final int port;
    private final int vendorId;
    private final int serialNumber;
    private final Duration timeout;
    private final ExecutorService executor;
    private final EventLoopGroup eventLoop;
    private final HashedWheelTimer wheelTimer;

    public EtherNetIpClientConfig(String hostname,
                                  int port,
                                  int vendorId,
                                  int serialNumber,
                                  Duration timeout,
                                  ExecutorService executor,
                                  EventLoopGroup eventLoop,
                                  HashedWheelTimer wheelTimer) {

        this.hostname = hostname;
        this.port = port;
        this.vendorId = vendorId;
        this.serialNumber = serialNumber;
        this.timeout = timeout;
        this.executor = executor;
        this.eventLoop = eventLoop;
        this.wheelTimer = wheelTimer;
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

    public ExecutorService getExecutor() {
        return executor;
    }

    public EventLoopGroup getEventLoop() {
        return eventLoop;
    }

    public HashedWheelTimer getWheelTimer() {
        return wheelTimer;
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
        private ExecutorService executor;
        private EventLoopGroup eventLoop;
        private HashedWheelTimer wheelTimer;

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

        public Builder setExecutor(ExecutorService executor) {
            this.executor = executor;
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

        public EtherNetIpClientConfig build() {
            if (executor == null) {
                executor = EtherNetIpShared.sharedExecutorService();
            }
            if (eventLoop == null) {
                eventLoop = EtherNetIpShared.sharedEventLoop();
            }
            if (wheelTimer == null) {
                wheelTimer = EtherNetIpShared.sharedWheelTimer();
            }
            return new EtherNetIpClientConfig(
                    hostname, port, vendorId, serialNumber,
                    timeout, executor, eventLoop, wheelTimer);
        }
    }

}
