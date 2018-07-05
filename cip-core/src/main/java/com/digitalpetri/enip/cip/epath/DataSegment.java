package com.digitalpetri.enip.cip.epath;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;

public abstract class DataSegment<T> extends EPathSegment {

    public static final int SEGMENT_TYPE = 0x80;

    protected abstract ByteBuf encode(ByteBuf buffer);

    public static ByteBuf encode(DataSegment<?> segment, boolean padded, ByteBuf buffer) {
        return segment.encode(buffer);
    }

    public static final class AnsiDataSegment extends DataSegment<String> {

        public static final int SUBTYPE = 0x11;

        private static final Charset ASCII = Charset.forName("US-ASCII");

        private final String data;

        public AnsiDataSegment(String data) {
            this.data = data;
        }

        @Override
        protected ByteBuf encode(ByteBuf buffer) {
            String data = this.data.length() <= 255 ?
                this.data : this.data.substring(0, 255);

            buffer.writeByte(SEGMENT_TYPE | SUBTYPE);
            buffer.writeByte(data.length());
            buffer.writeBytes(data.getBytes(ASCII));
            if (data.length() % 2 != 0) buffer.writeByte(0);

            return buffer;
        }

    }

    public static final class SimpleDataSegment extends DataSegment<short[]> {

        private final short[] data;

        public SimpleDataSegment(short[] data) {
            this.data = data;
        }

        @Override
        protected ByteBuf encode(ByteBuf buffer) {
            buffer.writeByte(SEGMENT_TYPE);
            buffer.writeByte(data.length);

            for (short d : data) {
                buffer.writeShort(d);
            }

            return buffer;
        }

    }

}
