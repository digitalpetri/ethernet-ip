package com.digitalpetri.enip.fsm.states;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.enip.fsm.ChannelFsm;
import com.digitalpetri.enip.fsm.events.ChannelIdle;
import com.digitalpetri.enip.fsm.events.ChannelInactive;
import com.digitalpetri.enip.fsm.events.ConnectSuccess;
import com.digitalpetri.enip.fsm.events.Disconnect;
import com.google.common.primitives.Ints;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connected extends AbstractState {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(ChannelFsm.class);

    @Override
    public ChannelFsm.State evaluate(
        ChannelFsm fsm,
        ChannelFsm.Event event) {

        if (event instanceof ChannelIdle) {
            sendKeepAliveAsync(fsm);

            return this;
        } else if (event instanceof ChannelInactive) {
            if (fsm.isLazy()) {
                return new Idle();
            } else {
                connectAsync(fsm);

                return new Reconnecting();
            }
        } else if (event instanceof Disconnect) {
            disconnectAsync(fsm, fsm.context().getChannelFuture());

            return new Disconnecting();
        } else {
            return this;
        }
    }

    @Override
    public void onExternalTransition(
        ChannelFsm fsm,
        ChannelFsm.State prev,
        ChannelFsm.Event event) {

        if (event instanceof ConnectSuccess) {
            Channel channel = ((ConnectSuccess) event).getChannel();

            int maxIdle = Ints.saturatedCast(fsm.getClient().getConfig().getMaxIdle().getSeconds());

            if (maxIdle > 0) {
                channel.pipeline().addLast(new IdleStateHandler(maxIdle, 0, 0));
            }

            channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                    LOGGER.debug("channelInactive() local={}, remote={}",
                        ctx.channel().localAddress(), ctx.channel().remoteAddress());

                    fsm.fireEvent(new ChannelInactive());

                    super.channelInactive(ctx);
                }

                @Override
                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                    if (evt instanceof IdleStateEvent) {
                        IdleState idleState = ((IdleStateEvent) evt).state();

                        if (idleState == IdleState.READER_IDLE) {
                            fsm.fireEvent(new ChannelIdle());
                        }
                    }

                    super.userEventTriggered(ctx, evt);
                }
            });

            CompletableFuture<Channel> future =
                fsm.context().getChannelFuture();

            future.complete(channel);
        }

        super.onExternalTransition(fsm, prev, event);
    }

}
