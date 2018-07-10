package com.digitalpetri.enip.fsm.events;

import com.digitalpetri.enip.fsm.ChannelFsm;

public class ConnectFailure implements ChannelFsm.Event {

    private final Throwable failure;

    public ConnectFailure(Throwable failure) {
        this.failure = failure;
    }

    public Throwable getFailure() {
        return failure;
    }

}
