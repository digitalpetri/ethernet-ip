package com.digitalpetri.enip.fsm.states;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.enip.fsm.ChannelFsm;
import com.digitalpetri.enip.fsm.events.Disconnect;
import com.digitalpetri.enip.fsm.events.DisconnectSuccess;

import static com.digitalpetri.enip.util.FutureUtils.complete;

public class Disconnecting implements ChannelFsm.State {

    private final Queue<ChannelFsm.Event> eventQueue = new ArrayDeque<>();

    @Override
    public ChannelFsm.State evaluate(ChannelFsm fsm, ChannelFsm.Event event) {
        if (event instanceof DisconnectSuccess) {
            fsm.getClient().getExecutor().submit(
                () -> eventQueue.forEach(fsm::fireEvent));

            return new NotConnected();
        } else {
            return this;
        }
    }

    @Override
    public void onInternalTransition(ChannelFsm fsm, ChannelFsm.Event event) {
        if (event instanceof Disconnect) {
            CompletableFuture<Void> future =
                ((Disconnect) event).getDisconnectFuture();

            complete(future).with(fsm.context().getDisconnectFuture());
        } else {
            eventQueue.add(event);
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
