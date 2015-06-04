package com.digitalpetri.enip;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.digitalpetri.enip.commands.Command;
import com.digitalpetri.enip.commands.CommandCode;
import com.digitalpetri.enip.commands.ListIdentity;
import com.digitalpetri.enip.commands.SendRRData;
import com.digitalpetri.enip.commands.SendUnitData;
import com.digitalpetri.enip.cpf.ConnectedDataItemResponse;
import com.digitalpetri.enip.cpf.CpfPacket;
import com.digitalpetri.enip.cpf.UnconnectedDataItemResponse;
import com.google.common.collect.Maps;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EtherNetIpClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ExecutorService executor;

    private final Map<Long, PendingRequest<? extends Command>> pendingRequests = Maps.newConcurrentMap();
    private final AtomicLong senderContext = new AtomicLong(0L);

    private volatile long sessionHandle;

    private final ChannelManager channelManager;

    private final EtherNetIpClientConfig config;

    public EtherNetIpClient(EtherNetIpClientConfig config) {
        this.config = config;

        executor = config.getExecutor();

        channelManager = new ChannelManager(this);
    }

    public CompletableFuture<EtherNetIpClient> connect() {
        CompletableFuture<EtherNetIpClient> future = new CompletableFuture<>();

        channelManager.getChannel().whenComplete((ch, ex) -> {
            if (ch != null) future.complete(EtherNetIpClient.this);
            else future.completeExceptionally(ex);
        });

        return future;
    }

    public CompletableFuture<EtherNetIpClient> disconnect() {
        channelManager.disconnect();

        return CompletableFuture.completedFuture(this);
    }

    public String getState() {
        return channelManager.getState();
    }

    public CompletableFuture<ListIdentity> listIdentity() {
        return sendCommand(new ListIdentity());
    }

    public CompletableFuture<SendRRData> sendRRData(SendRRData command) {
        return sendCommand(command);
    }

    public CompletableFuture<Void> sendUnitData(SendUnitData command) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        channelManager.getChannel().whenComplete((ch, ex) -> {
            if (ch != null) {
                EnipPacket packet = new EnipPacket(
                        command.getCommandCode(),
                        sessionHandle,
                        EnipStatus.EIP_SUCCESS,
                        0L,
                        command);

                ch.writeAndFlush(packet).addListener(f -> {
                    if (f.isSuccess()) future.complete(null);
                    else future.completeExceptionally(f.cause());
                });
            } else {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    public EtherNetIpClientConfig getConfig() {
        return config;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    protected <T extends Command> CompletableFuture<T> sendCommand(Command command) {
        CompletableFuture<T> future = new CompletableFuture<>();

        channelManager.getChannel().whenComplete((ch, ex) -> {
            if (ch != null) writeCommand(ch, command, future);
            else future.completeExceptionally(ex);
        });

        return future;
    }

    protected <T extends Command> void writeCommand(Channel channel,
                                                    Command command,
                                                    CompletableFuture<T> future) {

        EnipPacket packet = new EnipPacket(
                command.getCommandCode(),
                sessionHandle,
                EnipStatus.EIP_SUCCESS,
                senderContext.getAndIncrement(),
                command
        );

        Timeout timeout = config.getWheelTimer().newTimeout(tt -> {
            if (tt.isCancelled()) return;
            PendingRequest<?> p = pendingRequests.remove(packet.getSenderContext());
            if (p != null) {
                String message = String.format("senderContext=%s timed out waiting %sms for response",
                        packet.getSenderContext(), config.getTimeout().toMillis());
                p.promise.completeExceptionally(new Exception(message));
            }
        }, config.getTimeout().toMillis(), TimeUnit.MILLISECONDS);

        pendingRequests.put(packet.getSenderContext(), new PendingRequest<>(future, timeout));

        channel.writeAndFlush(packet).addListener(f -> {
            if (!f.isSuccess()) {
                PendingRequest pending = pendingRequests.remove(packet.getSenderContext());
                if (pending != null) {
                    pending.timeout.cancel();
                    pending.promise.completeExceptionally(f.cause());
                }
            }
        });
    }

    private void onChannelRead(EnipPacket packet) {
        CommandCode commandCode = packet.getCommandCode();

        if (commandCode == CommandCode.SendUnitData) {
            onUnitDataReceived((SendUnitData) packet.getCommand());
        } else {
            if (commandCode == CommandCode.RegisterSession) {
                EnipStatus status = packet.getStatus();

                if (status == EnipStatus.EIP_SUCCESS) {
                    sessionHandle = packet.getSessionHandle();
                } else {
                    sessionHandle = 0L;
                }
            }

            PendingRequest<?> pending = pendingRequests.remove(packet.getSenderContext());

            if (pending != null) {
                pending.timeout.cancel();

                EnipStatus status = packet.getStatus();

                if (status == EnipStatus.EIP_SUCCESS) {
                    pending.promise.complete(packet.getCommand());
                } else {
                    pending.promise.completeExceptionally(new Exception("EtherNet/IP status: " + status));
                }
            } else {
                logger.debug("Received response for unknown context: {}", packet.getSenderContext());

                if (packet.getCommand() instanceof SendRRData) {
                    CpfPacket cpfPacket = ((SendRRData) packet.getCommand()).getPacket();

                    Arrays.stream(cpfPacket.getItems()).forEach(item -> {
                        if (item instanceof ConnectedDataItemResponse) {
                            ReferenceCountUtil.safeRelease(((ConnectedDataItemResponse) item).getData());
                        } else if (item instanceof UnconnectedDataItemResponse) {
                            ReferenceCountUtil.safeRelease(((UnconnectedDataItemResponse) item).getData());
                        }
                    });
                }
            }
        }
    }

    private void onChannelInactive(ChannelHandlerContext ctx) {
        logger.debug("onChannelInactive() {} <-> {}",
                ctx.channel().localAddress(), ctx.channel().remoteAddress());
    }

    private void onExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.debug("onExceptionCaught() {} <-> {}",
                ctx.channel().localAddress(), ctx.channel().remoteAddress(), cause);

        channelManager.disconnect();
    }

    /**
     * Subclasses can override this to handle incoming
     * {@link com.digitalpetri.enip.commands.SendUnitData} commands.
     *
     * @param command the {@link com.digitalpetri.enip.commands.SendUnitData} command received.
     */
    protected void onUnitDataReceived(SendUnitData command) {
    }

    private static final class EtherNetIpClientHandler extends SimpleChannelInboundHandler<EnipPacket> {

        private final ExecutorService executor;

        private final EtherNetIpClient client;

        private EtherNetIpClientHandler(EtherNetIpClient client) {
            this.client = client;

            executor = client.getExecutor();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, EnipPacket packet) throws Exception {
            executor.execute(() -> client.onChannelRead(packet));
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            client.onChannelInactive(ctx);

            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            client.onExceptionCaught(ctx, cause);
        }

    }

    public static CompletableFuture<Channel> bootstrap(EtherNetIpClient client) {
        CompletableFuture<Channel> future = new CompletableFuture<>();
        EtherNetIpClientConfig config = client.getConfig();

        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(config.getEventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) config.getTimeout().toMillis())
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new EnipCodec());
                        ch.pipeline().addLast(new EtherNetIpClientHandler(client));
                    }
                });

        config.getBootstrapConsumer().accept(bootstrap);

        bootstrap.connect(config.getHostname(), config.getPort())
                .addListener((ChannelFuture f) -> {
                    if (f.isSuccess()) {
                        future.complete(f.channel());
                    } else {
                        future.completeExceptionally(f.cause());
                    }
                });


        return future;
    }

    private static final class PendingRequest<T> {

        private final CompletableFuture<Command> promise = new CompletableFuture<>();

        private final Timeout timeout;

        @SuppressWarnings("unchecked")
        private PendingRequest(CompletableFuture<T> future, Timeout timeout) {
            this.timeout = timeout;

            promise.whenComplete((r, ex) -> {
                if (r != null) {
                    try {
                        future.complete((T) r);
                    } catch (ClassCastException e) {
                        future.completeExceptionally(e);
                    }
                } else {
                    future.completeExceptionally(ex);
                }
            });
        }

    }

}
