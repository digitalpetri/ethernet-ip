package com.digitalpetri.enip.fsm.states;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.enip.fsm.ChannelFsm;
import com.digitalpetri.enip.fsm.events.Connect;
import com.digitalpetri.enip.fsm.events.Disconnect;
import com.digitalpetri.enip.fsm.events.DisconnectSuccess;
import com.digitalpetri.enip.fsm.events.GetChannel;
import io.netty.channel.Channel;

import static com.digitalpetri.enip.util.FutureUtils.complete;

public class Disconnecting extends AbstractState {

    @Override
    public ChannelFsm.State evaluate(ChannelFsm fsm, ChannelFsm.Event event) {
        if (event instanceof Connect) {
            connectAsync(fsm);

            return new Connecting();
        } else if (event instanceof DisconnectSuccess) {
            return new NotConnected();
        } else {
            return this;
        }
    }

    @Override
    public void onInternalTransition(ChannelFsm fsm, ChannelFsm.Event event) {
        if (event instanceof GetChannel) {
            CompletableFuture<Channel> future =
                ((GetChannel) event).getChannelFuture();

            complete(future).with(fsm.context().getChannelFuture());
        } else if (event instanceof Disconnect) {
            CompletableFuture<Void> future =
                ((Disconnect) event).getDisconnectFuture();

            complete(future).with(fsm.context().getDisconnectFuture());
        }
    }

    @Override
    public void onExternalTransition(ChannelFsm fsm, ChannelFsm.State prev, ChannelFsm.Event event) {
        if (event instanceof Disconnect) {
            CompletableFuture<Void> disconnectFuture =
                ((Disconnect) event).getDisconnectFuture();

            fsm.context().setDisconnectFuture(disconnectFuture);
        }
    }

}
