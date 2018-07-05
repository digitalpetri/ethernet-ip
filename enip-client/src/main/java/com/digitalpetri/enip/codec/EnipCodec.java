package com.digitalpetri.enip.codec;

import java.nio.ByteOrder;
import java.util.List;

import com.digitalpetri.enip.EnipPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

public class EnipCodec extends ByteToMessageCodec<EnipPacket> {

    private static final int HEADER_SIZE = 24;
    private static final int LENGTH_OFFSET = 2;

    @Override
    protected void encode(ChannelHandlerContext ctx, EnipPacket packet, ByteBuf out) throws Exception {
        EnipPacket.encode(packet, out.order(ByteOrder.LITTLE_ENDIAN));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ByteBuf buffer = in.order(ByteOrder.LITTLE_ENDIAN);

        int startIndex = buffer.readerIndex();

        while (buffer.readableBytes() >= HEADER_SIZE &&
            buffer.readableBytes() >= HEADER_SIZE + getLength(buffer, startIndex)) {

            out.add(EnipPacket.decode(buffer));

            startIndex = buffer.readerIndex();
        }
    }

    private int getLength(ByteBuf buffer, int startIndex) {
        return buffer.getUnsignedShort(startIndex + LENGTH_OFFSET);
    }

}
