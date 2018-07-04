package com.digitalpetri.enip.cpf;

import io.netty.buffer.ByteBuf;

public final class SockAddrItemO2t extends CpfItem {

    public static final int TYPE_ID = 0x8000;

    private final SockAddr sockAddr;

    public SockAddrItemO2t(SockAddr sockAddr) {
        super(TYPE_ID);

        this.sockAddr = sockAddr;
    }

    public SockAddr getSockAddr() {
        return sockAddr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SockAddrItemO2t that = (SockAddrItemO2t) o;

        return sockAddr.equals(that.sockAddr);
    }

    @Override
    public int hashCode() {
        return sockAddr.hashCode();
    }

    private static final int ITEM_LENGTH = 16;

    public static ByteBuf encode(SockAddrItemO2t item, ByteBuf buffer) {
        buffer.writeShort(TYPE_ID);
        buffer.writeShort(ITEM_LENGTH);

        return SockAddr.encode(item.getSockAddr(), buffer);
    }

    public static SockAddrItemO2t decode(ByteBuf buffer) {
        int typeId = buffer.readUnsignedShort();
        int length = buffer.readUnsignedShort();

        assert (typeId == TYPE_ID);
        assert (length == ITEM_LENGTH);

        SockAddr sockAddr = SockAddr.decode(buffer);

        return new SockAddrItemO2t(sockAddr);
    }

}
