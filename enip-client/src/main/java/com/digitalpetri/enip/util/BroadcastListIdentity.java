package com.digitalpetri.enip.util;

import com.digitalpetri.enip.EtherNetIpShared;
import com.digitalpetri.enip.EnipCodec;
import com.digitalpetri.enip.EnipPacket;
import com.digitalpetri.enip.EnipStatus;
import com.digitalpetri.enip.commands.ListIdentity;
import com.digitalpetri.enip.cpf.CipIdentityItem;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToMessageDecoder;

public class BroadcastListIdentity {

    private static final String TAG = "ListIdentityBroadcast";

    private static final int PORT = 44818;

    private Consumer<Bootstrap> bootstrapConsumer;
    private Consumer<CipIdentityItem> identityConsumer;

    public BroadcastListIdentity(@NonNull Consumer<CipIdentityItem> identityConsumer) {
        this(identityConsumer, (b) -> {});
    }

    public BroadcastListIdentity(@NonNull Consumer<CipIdentityItem> identityConsumer, Consumer<Bootstrap> bootstrapConsumer) {
        this.identityConsumer = identityConsumer;
        this.bootstrapConsumer = bootstrapConsumer;
    }

    public void listIdentity(DatagramChannel channel, String broadcastAddress) {
        this.listIdentity(channel, new InetSocketAddress(broadcastAddress, PORT));
    }

    public void listIdentity(DatagramChannel channel, InetSocketAddress address) {
        if (channel != null) {
            ListIdentity li = new ListIdentity();

            EnipPacket packet = new EnipPacket(
                    li.getCommandCode(),
                    0L,
                    EnipStatus.EIP_SUCCESS,
                    0L,
                    li
            );

            ByteBuf buf = Unpooled.buffer().order(ByteOrder.LITTLE_ENDIAN);
            EnipPacket.encode(packet, buf);

            channel.writeAndFlush(new DatagramPacket(buf, address));
            buf.release();
        }
    }

    public static CompletableFuture<DatagramChannel> bootstrap(BroadcastListIdentity bli) {
        CompletableFuture<DatagramChannel> future = new CompletableFuture<>();

        Bootstrap b = new Bootstrap();

        b.group(EtherNetIpShared.sharedEventLoop())
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .option(ChannelOption.SO_REUSEADDR, true)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                public void initChannel(NioDatagramChannel ch) throws Exception {
                    ch.pipeline().addLast(new DatagramPacketToByteDecoder());
                    ch.pipeline().addLast(new EnipCodec());
                    ch.pipeline().addLast(new BroadcastHandler(bli));
                }
            });

        bli.bootstrapConsumer.accept(b);

        b.bind(0).addListener((ChannelFuture f) -> {
                if (f.isSuccess()) {
                    future.complete((DatagramChannel) f.channel());
                } else {
                    future.completeExceptionally(f.cause());
                }
            });

        return future;
    }

    private static class DatagramPacketToByteDecoder extends MessageToMessageDecoder<DatagramPacket> {

        @Override
        protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
            out.add(msg.content().retain());
        }
    }

    private static class BroadcastHandler extends SimpleChannelInboundHandler<EnipPacket> {

        private BroadcastListIdentity bli;

        private BroadcastHandler(BroadcastListIdentity bli) {
            this.bli = bli;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, EnipPacket msg) throws Exception {
            if (msg.getCommand() instanceof ListIdentity) {
                ListIdentity li = (ListIdentity) msg.getCommand();
                li.getIdentity().ifPresent(id -> this.bli.identityConsumer.accept(id));
            }
        }
    }
}
