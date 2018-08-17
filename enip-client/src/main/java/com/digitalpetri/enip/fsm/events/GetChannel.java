package com.digitalpetri.enip.fsm.events;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.enip.fsm.ChannelFsm;
import io.netty.channel.Channel;

public class GetChannel implements ChannelFsm.Event {

    private final CompletableFuture<Channel> channelFuture = new CompletableFuture<>();

    public CompletableFuture<Channel> getChannelFuture() {
        return channelFuture;
    }

}
