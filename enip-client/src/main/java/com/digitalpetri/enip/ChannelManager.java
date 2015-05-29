package com.digitalpetri.enip;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.digitalpetri.enip.commands.RegisterSession;
import com.digitalpetri.enip.commands.UnRegisterSession;
import io.netty.channel.Channel;

public class ChannelManager {

    private final AtomicReference<State> state = new AtomicReference<>(new Idle());

    private final EtherNetIpClient client;

    public ChannelManager(EtherNetIpClient client) {
        this.client = client;
    }

    public CompletableFuture<Channel> getChannel() {
        State currentState = state.get();

        if (currentState instanceof Idle) {
            Connecting nextState = new Connecting();

            if (state.compareAndSet(currentState, nextState)) {
                CompletableFuture<Channel> future = nextState.future;

                future.whenComplete((ch, ex) -> {
                    if (ch != null) state.set(new Connected(future));
                    else state.set(new Idle());
                });

                return connect(future);
            } else {
                return getChannel();
            }
        } else if (currentState instanceof Connecting) {
            return ((Connecting) currentState).future;
        } else if (currentState instanceof Connected) {
            return ((Connected) currentState).future;
        } else {
            throw new IllegalStateException(currentState.getClass().getSimpleName());
        }
    }

    private CompletableFuture<Channel> connect(CompletableFuture<Channel> future) {
        CompletableFuture<Channel> bootstrap = EtherNetIpClient.bootstrap(client);

        bootstrap.whenComplete((ch, ex) -> {
            if (ch != null) {
                ch.closeFuture().addListener(f -> state.set(new Idle()));

                CompletableFuture<RegisterSession> registerFuture = new CompletableFuture<>();

                registerFuture.whenComplete((r, ex2) -> {
                    if (r != null) future.complete(ch);
                    else future.completeExceptionally(ex2);
                });

                client.writeCommand(ch, new RegisterSession(), registerFuture);
            } else {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    public void disconnect() {
        State currentState = state.get();

        if (currentState instanceof Connecting) {
            ((Connecting) currentState).future.whenComplete((ch, ex) -> {
                if (ch != null) ch.close();
            });
        } else if (currentState instanceof Connected) {
            ((Connected) currentState).future.whenComplete((ch, ex) -> {
                CompletableFuture<UnRegisterSession> f = new CompletableFuture<>();
                client.writeCommand(ch, new UnRegisterSession(), f);

                f.whenComplete((cmd, ex2) -> ch.close());
            });
        }
    }

    public String getState() {
        return state.get().getClass().getSimpleName();
    }

    private static abstract class State {
    }

    private static class Idle extends State {
    }

    private static class Connecting extends State {
        private final CompletableFuture<Channel> future = new CompletableFuture<>();
    }

    private static class Connected extends State {
        private final CompletableFuture<Channel> future;

        private Connected(CompletableFuture<Channel> future) {
            this.future = future;
        }
    }

}
