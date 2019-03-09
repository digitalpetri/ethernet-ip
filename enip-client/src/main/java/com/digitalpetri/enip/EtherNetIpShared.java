package com.digitalpetri.enip;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;

public class EtherNetIpShared {

    private static EventLoopGroup SHARED_EVENT_LOOP;
    private static HashedWheelTimer SHARED_WHEEL_TIMER;
    private static ExecutorService SHARED_EXECUTOR;
    private static ScheduledExecutorService SHARED_SCHEDULED_EXECUTOR;

    /**
     * @return a shared {@link io.netty.channel.EventLoopGroup}.
     */
    public static synchronized EventLoopGroup sharedEventLoop() {
        if (SHARED_EVENT_LOOP == null) {
            SHARED_EVENT_LOOP = new NioEventLoopGroup();
        }
        return SHARED_EVENT_LOOP;
    }

    /**
     * @return a shared {@link io.netty.util.HashedWheelTimer}.
     */
    public static synchronized HashedWheelTimer sharedWheelTimer() {
        if (SHARED_WHEEL_TIMER == null) {
            SHARED_WHEEL_TIMER = new HashedWheelTimer();
        }
        return SHARED_WHEEL_TIMER;
    }

    /**
     * @return a shared {@link java.util.concurrent.ExecutorService}.
     */
    public static synchronized ExecutorService sharedExecutorService() {
        if (SHARED_EXECUTOR == null) {
            SHARED_EXECUTOR = Executors.newWorkStealingPool();
        }
        return SHARED_EXECUTOR;
    }

    /**
     * @return a shared {@link ScheduledExecutorService}.
     */
    public static synchronized ScheduledExecutorService sharedScheduledExecutor() {
        if (SHARED_SCHEDULED_EXECUTOR == null) {
            SHARED_SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
        }
        return SHARED_SCHEDULED_EXECUTOR;
    }

    /**
     * Release/shutdown/cleanup any shared resources that were created.
     */
    public static synchronized void releaseSharedResources() {
        if (SHARED_EVENT_LOOP != null) {
            SHARED_EVENT_LOOP.shutdownGracefully();
            SHARED_EVENT_LOOP = null;
        }
        if (SHARED_WHEEL_TIMER != null) {
            SHARED_WHEEL_TIMER.stop();
            SHARED_WHEEL_TIMER = null;
        }
        if (SHARED_EXECUTOR != null) {
            SHARED_EXECUTOR.shutdown();
            SHARED_EXECUTOR = null;
        }
        if (SHARED_SCHEDULED_EXECUTOR != null) {
            SHARED_SCHEDULED_EXECUTOR.shutdown();
            SHARED_SCHEDULED_EXECUTOR = null;
        }
    }

}
