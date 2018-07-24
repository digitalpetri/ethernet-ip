package com.digitalpetri.enip.fsm;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.digitalpetri.enip.EtherNetIpClient;
import com.digitalpetri.enip.EtherNetIpClientConfig;
import com.digitalpetri.enip.fsm.events.ConnectFailure;
import com.digitalpetri.enip.fsm.states.Connecting;
import com.digitalpetri.enip.fsm.states.Reconnecting;
import io.netty.channel.Channel;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ChannelFsmTest {

    @Test
    public void testLazyBehavior() {
        // a Lazy ChannelFsm goes from Reconnecting to Idle on a ConnectFailure event
        EtherNetIpClient client = newTestClient(b -> {
            b.setLazy(true);
            b.setPersistent(false);
        });

        ChannelFsm fsm = new ChannelFsm(client, new Reconnecting());
        fsm.context().setChannelFuture(new CompletableFuture<>());

        fsm.fireEvent(new ConnectFailure(new Exception("test failure")));

        assertEquals(fsm.getState(), "Idle");
        assertTrue(fsm.context().getChannelFuture().isCompletedExceptionally());
    }

    @Test
    public void testPersistentBehavior() {
        // a Persistent ChannelFsm goes from Connecting to Reconnecting on a ConnectFailure event.
        EtherNetIpClient client = newTestClient(b -> {
            b.setLazy(false);
            b.setPersistent(true);
        });

        ChannelFsm fsm = new ChannelFsm(client, new Connecting());

        CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
        fsm.context().setChannelFuture(channelFuture);

        fsm.fireEvent(new ConnectFailure(new Exception("test failure")));

        assertEquals(fsm.getState(), "Reconnecting");

        // Previous channel future gets completed exceptionally
        assertTrue(channelFuture.isCompletedExceptionally());

        // Transition to Reconnecting sets up a new channel future
        assertFalse(fsm.context().getChannelFuture().isDone());
    }

    @Test
    public void testLazyPersistentBehavior() {
        // a Lazy+Persistent ChannelFsm goes from (Connecting|Reconnecting) to Idle on a ConnectFailure event.
        EtherNetIpClient client = newTestClient(b -> {
            b.setLazy(true);
            b.setPersistent(true);
        });

        // S(Connecting) x E(ConnectionFailure) = S'(Idle)
        {
            ChannelFsm fsm = new ChannelFsm(client, new Connecting());

            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            fsm.context().setChannelFuture(channelFuture);

            fsm.fireEvent(new ConnectFailure(new Exception("test failure")));
            assertEquals(fsm.getState(), "Idle");
            assertTrue(fsm.context().getChannelFuture().isCompletedExceptionally());
        }

        // S(Reconnecting) x E(ConnectionFailure) = S'(Idle)
        {
            ChannelFsm fsm = new ChannelFsm(client, new Reconnecting());

            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            fsm.context().setChannelFuture(channelFuture);

            fsm.fireEvent(new ConnectFailure(new Exception("test failure")));
            assertEquals(fsm.getState(), "Idle");
            assertTrue(fsm.context().getChannelFuture().isCompletedExceptionally());
        }
    }

    private static EtherNetIpClient newTestClient(Consumer<EtherNetIpClientConfig.Builder> consumer) {
        EtherNetIpClientConfig.Builder configBuilder =
            EtherNetIpClientConfig.builder("")
                .setSerialNumber(0x00)
                .setVendorId(0x00)
                .setTimeout(Duration.ofSeconds(2));

        consumer.accept(configBuilder);

        return new EtherNetIpClient(configBuilder.build());
    }

}
