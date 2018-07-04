package com.digitalpetri.enip.cip.epath;

import com.digitalpetri.enip.cip.structs.ElectronicKey;
import io.netty.buffer.ByteBuf;

public abstract class LogicalSegment<T> extends EPathSegment {

    public static final int SEGMENT_TYPE = 0x01;

    private final T value;
    private final LogicalType type;
    private final LogicalFormat format;

    protected LogicalSegment(T value, LogicalType type, LogicalFormat format) {
        this.value = value;
        this.type = type;
        this.format = format;
    }

    public T getValue() {
        return value;
    }

    public LogicalType getType() {
        return type;
    }

    public LogicalFormat getFormat() {
        return format;
    }

    protected abstract ByteBuf encodeValue(ByteBuf buffer);

    protected static LogicalFormat smallestFormat(int value) {
        if ((value & 0xFF) == value) {
            return LogicalFormat.Bits_8;
        } else if ((value & 0xFFFF) == value) {
            return LogicalFormat.Bits_16;
        } else {
            return LogicalFormat.Bits_32;
        }
    }

    public static ByteBuf encode(LogicalSegment<?> segment, boolean padded, ByteBuf buffer) {
        int segmentByte = 0;

        segmentByte |= (SEGMENT_TYPE << 5);
        segmentByte |= (segment.getType().getType() << 2);
        segmentByte |= segment.getFormat().getType();

        buffer.writeByte(segmentByte);

        if (padded && (segment.getFormat() == LogicalFormat.Bits_16 || segment.getFormat() == LogicalFormat.Bits_32)) {
            buffer.writeByte(0x00);
        }

        segment.encodeValue(buffer);

        return buffer;
    }

    private static ByteBuf encodeIntValue(LogicalFormat format, int value, ByteBuf buffer) {
        switch (format) {
            case Bits_8:
                return buffer.writeByte(value);
            case Bits_16:
                return buffer.writeShort(value);
            case Bits_32:
                return buffer.writeInt(value);
            case Reserved:
            default:
                throw new IllegalStateException("Reserved segment type not supported");
        }
    }

    private static ByteBuf encodeKeyValue(LogicalFormat format, ElectronicKey value, ByteBuf buffer) {
        return ElectronicKey.encode(value, buffer);
    }

    public static final class ClassId extends LogicalSegment<Integer> {

        public ClassId(Integer value) {
            this(value, smallestFormat(value));
        }

        public ClassId(int value, LogicalFormat format) {
            super(value, LogicalType.ClassId, format);
        }

        @Override
        protected ByteBuf encodeValue(ByteBuf buffer) {
            return encodeIntValue(getFormat(), getValue(), buffer);
        }

    }

    public static final class InstanceId extends LogicalSegment<Integer> {

        public InstanceId(int value) {
            this(value, smallestFormat(value));
        }

        public InstanceId(int value, LogicalFormat format) {
            super(value, LogicalType.InstanceId, format);
        }

        @Override
        protected ByteBuf encodeValue(ByteBuf buffer) {
            return encodeIntValue(getFormat(), getValue(), buffer);
        }

    }

    public static final class MemberId extends LogicalSegment<Integer> {

        public MemberId(Integer value) {
            this(value, smallestFormat(value));
        }

        public MemberId(Integer value, LogicalFormat format) {
            super(value, LogicalType.MemberId, format);
        }

        @Override
        protected ByteBuf encodeValue(ByteBuf buffer) {
            return encodeIntValue(getFormat(), getValue(), buffer);
        }

    }

    public static final class ConnectionPoint extends LogicalSegment<Integer> {

        public ConnectionPoint(Integer value) {
            this(value, smallestFormat(value));
        }

        public ConnectionPoint(Integer value, LogicalFormat format) {
            super(value, LogicalType.ConnectionPoint, format);
        }

        @Override
        protected ByteBuf encodeValue(ByteBuf buffer) {
            return encodeIntValue(getFormat(), getValue(), buffer);
        }

    }

    public static final class AttributeId extends LogicalSegment<Integer> {

        public AttributeId(Integer value) {
            super(value, LogicalType.AttributeId, smallestFormat(value));
        }

        public AttributeId(Integer value, LogicalFormat format) {
            super(value, LogicalType.AttributeId, format);
        }

        @Override
        protected ByteBuf encodeValue(ByteBuf buffer) {
            return encodeIntValue(getFormat(), getValue(), buffer);
        }

    }

    public static final class ServiceId extends LogicalSegment<Integer> {

        public ServiceId(Integer value, LogicalFormat format) {
            super(value, LogicalType.ServiceId, format);
        }

        @Override
        protected ByteBuf encodeValue(ByteBuf buffer) {
            return encodeIntValue(getFormat(), getValue(), buffer);
        }

    }

    public static final class KeySegment extends LogicalSegment<ElectronicKey> {

        public KeySegment(ElectronicKey value, LogicalFormat format) {
            super(value, LogicalType.Special, format);
        }

        @Override
        protected ByteBuf encodeValue(ByteBuf buffer) {
            return encodeKeyValue(getFormat(), getValue(), buffer);
        }

    }

    public static enum LogicalType {

        ClassId(0x0),
        InstanceId(0x1),
        MemberId(0x2),
        ConnectionPoint(0x3),
        AttributeId(0x4),
        Special(0x5),
        ServiceId(0x6),
        Reserved(0x7);

        private final int type;

        LogicalType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

    }

    public static enum LogicalFormat {

        Bits_8(0x0),
        Bits_16(0x1),
        Bits_32(0x2),
        Reserved(0x3);

        private final int type;

        LogicalFormat(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

    }

}
