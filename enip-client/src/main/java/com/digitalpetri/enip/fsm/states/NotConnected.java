package com.digitalpetri.enip.fsm.states;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.enip.fsm.ChannelFsm;
import com.digitalpetri.enip.fsm.events.Connect;
import com.digitalpetri.enip.fsm.events.ConnectFailure;
import com.digitalpetri.enip.fsm.events.Disconnect;
import com.digitalpetri.enip.fsm.events.DisconnectSuccess;
import com.digitalpetri.enip.fsm.events.GetChannel;
import io.netty.channel.Channel;

public class NotConnected extends AbstractState {

    @Override
    public ChannelFsm.State evaluate(
        ChannelFsm fsm,
        ChannelFsm.Event event) {

        if (event instanceof Connect) {
            connectAsync(fsm);

            return new Connecting();
        } else {
            return this;
        }
    }

    @Override
    public void onInternalTransition(
        ChannelFsm fsm,
        ChannelFsm.Event event) {

        if (event instanceof Disconnect) {
            CompletableFuture<Void> future =
                ((Disconnect) event).getDisconnectFuture();

            future.complete(null);
        } else if (event instanceof GetChannel) {
            CompletableFuture<Channel> future =
                ((GetChannel) event).getChannelFuture();

            future.completeExceptionally(new Exception("not connected"));
        }
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
        } else if (event instanceof DisconnectSuccess) {
            CompletableFuture<Void> future =
                fsm.context().getDisconnectFuture();

            fsm.context().setDisconnectFuture(null);

            future.complete(null);
        }
    }

}
