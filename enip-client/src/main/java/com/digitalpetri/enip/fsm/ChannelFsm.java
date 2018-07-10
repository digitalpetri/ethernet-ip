package com.digitalpetri.enip.fsm;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.digitalpetri.enip.EtherNetIpClient;
import com.digitalpetri.enip.fsm.events.Connect;
import com.digitalpetri.enip.fsm.events.Disconnect;
import com.digitalpetri.enip.fsm.events.GetChannel;
import com.digitalpetri.enip.fsm.states.Connected;
import com.digitalpetri.enip.fsm.states.NotConnected;
import io.netty.channel.Channel;
import io.netty.util.DefaultAttributeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.digitalpetri.enip.util.FutureUtils.complete;

public class ChannelFsm extends DefaultAttributeMap implements Fsm<ChannelFsm.Event> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicReference<State> state = new AtomicReference<>(new NotConnected());

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

    private final Context context = new Context();

    private final boolean lazy;
    private final boolean persistent;

    private final EtherNetIpClient client;

    public ChannelFsm(EtherNetIpClient client) {
        this.client = client;

        this.lazy = client.getConfig().isLazy();
        this.persistent = client.getConfig().isPersistent();
    }

    public CompletableFuture<Channel> connect() {
        Connect connect = new Connect();

        fireEvent(connect);

        return complete(new CompletableFuture<Channel>())
            .async(client.getExecutor())
            .with(connect.getChannelFuture());
    }

    public CompletableFuture<Void> disconnect() {
        Disconnect disconnect = new Disconnect();

        fireEvent(disconnect);

        return complete(new CompletableFuture<Void>())
            .async(client.getExecutor())
            .with(disconnect.getDisconnectFuture());
    }

    public CompletableFuture<Channel> getChannel() {
        State current;

        try {
            readWriteLock.readLock().lock();
            current = state.get();
        } finally {
            readWriteLock.readLock().unlock();
        }

        if (current instanceof Connected) {
            // "Fast" path... already connected.

            return context.getChannelFuture();
        } else {
            // "Slow" path... still connecting.

            GetChannel getChannel = new GetChannel();

            fireEvent(getChannel);

            return complete(new CompletableFuture<Channel>())
                .async(client.getExecutor())
                .with(getChannel.getChannelFuture());
        }
    }

    public String getState() {
        return state.get().getClass().getSimpleName();
    }

    public Context context() {
        return context;
    }

    public EtherNetIpClient getClient() {
        return client;
    }

    public boolean isLazy() {
        return lazy;
    }

    public boolean isPersistent() {
        return persistent;
    }

    @Override
    public void fireEvent(Event event) {
        if (readWriteLock.writeLock().isHeldByCurrentThread()) {
            client.getExecutor()
                .submit(() -> fireEvent(event));
        } else {
            readWriteLock.writeLock().lock();

            try {
                State prevState = state.get();

                State nextState = state.updateAndGet(
                    state ->
                        state.evaluate(this, event)
                );

                logger.debug(
                    "S({}) x E({}) = S'({})",
                    prevState.getClass().getSimpleName(),
                    event.getClass().getSimpleName(),
                    nextState.getClass().getSimpleName()
                );

                if (prevState.getClass() == nextState.getClass()) {
                    nextState.onInternalTransition(this, event);
                } else {
                    nextState.onExternalTransition(this, prevState, event);
                }

            } finally {
                readWriteLock.writeLock().unlock();
            }
        }
    }

    public interface Event {}

    public interface State extends Fsm.State<ChannelFsm, State, Event> {}

    public static class Context {

        private CompletableFuture<Channel> channelFuture;
        private CompletableFuture<Void> disconnectFuture;
        private long reconnectDelay;

        public CompletableFuture<Channel> getChannelFuture() {
            return channelFuture;
        }

        public void setChannelFuture(CompletableFuture<Channel> channelFuture) {
            this.channelFuture = channelFuture;
        }

        public CompletableFuture<Void> getDisconnectFuture() {
            return disconnectFuture;
        }

        public void setDisconnectFuture(CompletableFuture<Void> disconnectFuture) {
            this.disconnectFuture = disconnectFuture;
        }

        public long getReconnectDelay() {
            return reconnectDelay;
        }

        public void setReconnectDelay(long reconnectDelay) {
            this.reconnectDelay = reconnectDelay;
        }

    }

}
