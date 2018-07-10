package com.digitalpetri.enip.fsm.states;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.enip.fsm.ChannelFsm;
import com.digitalpetri.enip.fsm.events.ConnectFailure;
import com.digitalpetri.enip.fsm.events.ConnectSuccess;

public class Reconnecting extends AbstractState {

    private static final int MAX_RECONNECT_DELAY = 32; // seconds

    @Override
    public ChannelFsm.State evaluate(ChannelFsm fsm, ChannelFsm.Event event) {
        if (event instanceof ConnectSuccess) {
            return new Connected();
        } else if (event instanceof ConnectFailure) {
            if (fsm.isLazy()) {
                return new Idle();
            } else {
                long delay = fsm.context().getReconnectDelay();

                reconnectAsync(fsm, delay);

                return new Reconnecting();
            }
        } else {
            return this;
        }
    }

    @Override
    public void onInternalTransition(
        ChannelFsm fsm,
        ChannelFsm.Event event) {

        if (event instanceof ConnectFailure) {
            Throwable failure = ((ConnectFailure) event).getFailure();

            fsm.context().getChannelFuture()
                .completeExceptionally(failure);

            fsm.context().setChannelFuture(new CompletableFuture<>());

            long delay = fsm.context().getReconnectDelay();

            fsm.context().setReconnectDelay(
                Math.min(MAX_RECONNECT_DELAY, delay << 1L));
        }

        super.onInternalTransition(fsm, event);
    }

    @Override
    public void onExternalTransition(
        ChannelFsm fsm,
        ChannelFsm.State prev,
        ChannelFsm.Event event) {

        if (event instanceof ConnectFailure) {
            Throwable failure = ((ConnectFailure) event).getFailure();

            fsm.context().getChannelFuture()
                .completeExceptionally(failure);
        }

        fsm.context().setChannelFuture(new CompletableFuture<>());

        fsm.context().setReconnectDelay(1L);

        super.onExternalTransition(fsm, prev, event);
    }

}

