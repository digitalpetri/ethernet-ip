package com.digitalpetri.enip.fsm.states;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.enip.fsm.ChannelFsm;
import com.digitalpetri.enip.fsm.events.ConnectFailure;
import com.digitalpetri.enip.fsm.events.ConnectSuccess;
import com.digitalpetri.enip.fsm.events.Disconnect;

public class Connecting extends AbstractState {

    @Override
    public ChannelFsm.State evaluate(
        ChannelFsm fsm,
        ChannelFsm.Event event) {

        if (event instanceof ConnectSuccess) {
            return new Connected();
        } else if (event instanceof ConnectFailure) {
            if (fsm.isPersistent()) {
                if (fsm.isLazy()) {
                    return new Idle();
                } else {
                    connectAsync(fsm);

                    return new Reconnecting();
                }
            } else {
                return new NotConnected();
            }
        } else if (event instanceof Disconnect) {
            disconnectAsync(fsm, fsm.context().getChannelFuture());

            return new Disconnecting();
        } else {
            return this;
        }
    }

    @Override
    public void onExternalTransition(
        ChannelFsm fsm,
        ChannelFsm.State prev,
        ChannelFsm.Event event) {

        fsm.context().setChannelFuture(new CompletableFuture<>());

        super.onExternalTransition(fsm, prev, event);
    }

}
