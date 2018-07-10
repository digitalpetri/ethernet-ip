package com.digitalpetri.enip.fsm.states;

import com.digitalpetri.enip.fsm.ChannelFsm;
import com.digitalpetri.enip.fsm.events.Connect;
import com.digitalpetri.enip.fsm.events.ConnectFailure;
import com.digitalpetri.enip.fsm.events.Disconnect;
import com.digitalpetri.enip.fsm.events.GetChannel;

public class Idle extends AbstractState {

    @Override
    public ChannelFsm.State evaluate(ChannelFsm fsm, ChannelFsm.Event event) {
        if (event instanceof Connect) {
            connectAsync(fsm);

            return new Reconnecting();
        } else if (event instanceof GetChannel) {
            connectAsync(fsm);

            return new Reconnecting();
        } else if (event instanceof Disconnect) {
            return new NotConnected();
        } else {
            return this;
        }
    }

    @Override
    public void onExternalTransition(ChannelFsm fsm, ChannelFsm.State prev, ChannelFsm.Event event) {
        if (event instanceof ConnectFailure) {
            Throwable failure = ((ConnectFailure) event).getFailure();

            fsm.context().getChannelFuture()
                .completeExceptionally(failure);
        }

        super.onExternalTransition(fsm, prev, event);
    }
}
