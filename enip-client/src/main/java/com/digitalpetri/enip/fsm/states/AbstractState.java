package com.digitalpetri.enip.fsm.states;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.digitalpetri.enip.EtherNetIpClient;
import com.digitalpetri.enip.commands.RegisterSession;
import com.digitalpetri.enip.commands.UnRegisterSession;
import com.digitalpetri.enip.fsm.ChannelFsm;
import com.digitalpetri.enip.fsm.events.Connect;
import com.digitalpetri.enip.fsm.events.ConnectFailure;
import com.digitalpetri.enip.fsm.events.ConnectSuccess;
import com.digitalpetri.enip.fsm.events.DisconnectSuccess;
import com.digitalpetri.enip.fsm.events.GetChannel;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.digitalpetri.enip.util.FutureUtils.complete;

abstract class AbstractState implements ChannelFsm.State {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelFsm.class);

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onInternalTransition(
        ChannelFsm fsm,
        ChannelFsm.Event event) {

        onTransitionCausedByConnectOrGet(fsm, event);
    }

    @Override
    public void onExternalTransition(
        ChannelFsm fsm,
        ChannelFsm.State prev,
        ChannelFsm.Event event) {

        onTransitionCausedByConnectOrGet(fsm, event);
    }

    private static void onTransitionCausedByConnectOrGet(ChannelFsm fsm, ChannelFsm.Event event) {
        CompletableFuture<Channel> channelFuture =
            fsm.context().getChannelFuture();

        if (event instanceof Connect) {
            CompletableFuture<Channel> future =
                ((Connect) event).getChannelFuture();

            complete(future).with(channelFuture);
        } else if (event instanceof GetChannel) {
            CompletableFuture<Channel> future =
                ((GetChannel) event).getChannelFuture();

            complete(future).with(channelFuture);
        }
    }

    static void connectAsync(ChannelFsm fsm) {
        fsm.getClient().getExecutor().submit(() -> connect(fsm));
    }

    private static void connect(ChannelFsm fsm) {
        EtherNetIpClient.bootstrap(fsm.getClient())
            .thenCompose(channel -> {
                CompletableFuture<RegisterSession> future = new CompletableFuture<>();

                fsm.getClient().writeCommand(channel, new RegisterSession(), future);

                return future.thenApply(rs -> channel);
            })
            .whenComplete((channel, ex) -> {
                if (channel != null) {
                    LOGGER.debug(
                        "Connect succeeded: localAddress={}, remoteAddress={}",
                        channel.localAddress(), channel.remoteAddress());

                    fsm.fireEvent(new ConnectSuccess(channel));
                } else {
                    LOGGER.debug("Connect failed: {}", ex.getMessage(), ex);

                    fsm.fireEvent(new ConnectFailure(ex));
                }
            });
    }

    static void disconnectAsync(ChannelFsm fsm, CompletableFuture<Channel> channelFuture) {
        fsm.getClient().getExecutor().submit(() -> disconnect(fsm, channelFuture));
    }

    private static void disconnect(ChannelFsm fsm, CompletableFuture<Channel> channelFuture) {
        channelFuture
            .thenCompose(channel -> {
                CompletableFuture<UnRegisterSession> future = new CompletableFuture<>();
                fsm.getClient().writeCommand(channel, new UnRegisterSession(), future);

                return future.whenComplete((cmd, ex2) -> channel.close());
            })
            .whenComplete((urs, ex) ->
                fsm.fireEvent(new DisconnectSuccess())
            );
    }

    static void reconnectAsync(ChannelFsm fsm, long delaySeconds) {
        LOGGER.debug("Scheduling reconnect for +{} seconds", delaySeconds);

        SCHEDULER.schedule(
            () -> connectAsync(fsm),
            delaySeconds, TimeUnit.SECONDS
        );
    }

    static void sendKeepAliveAsync(ChannelFsm fsm) {
        LOGGER.debug("Sending keep alive via ListIdentity");

        CompletableFuture<Channel> channelFuture = fsm.context().getChannelFuture();

        fsm.getClient().getExecutor().submit(() -> sendKeepAlive(fsm, channelFuture));
    }

    private static void sendKeepAlive(ChannelFsm fsm, CompletableFuture<Channel> channelFuture) {
        fsm.getClient().listIdentity().whenComplete((li, ex) -> {
            if (ex != null) {
                LOGGER.debug("Keep alive failed; closing channel: {}", ex.getMessage());

                channelFuture.thenApply(Channel::close);
            }
        });
    }

}
