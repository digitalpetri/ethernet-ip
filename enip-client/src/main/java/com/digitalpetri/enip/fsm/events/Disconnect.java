package com.digitalpetri.enip.fsm.events;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.enip.fsm.ChannelFsm;

public class Disconnect implements ChannelFsm.Event {

    private final CompletableFuture<Void> future = new CompletableFuture<>();

    public CompletableFuture<Void> getDisconnectFuture() {
        return future;
    }

}
