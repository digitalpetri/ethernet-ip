package com.digitalpetri.enip.fsm.events;

import com.digitalpetri.enip.fsm.ChannelFsm;
import io.netty.channel.Channel;

public class ConnectSuccess implements ChannelFsm.Event {

    private final Channel channel;

    public ConnectSuccess(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

}
