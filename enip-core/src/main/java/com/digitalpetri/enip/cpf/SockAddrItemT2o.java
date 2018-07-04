package com.digitalpetri.enip.cpf;

import io.netty.buffer.ByteBuf;

public final class SockAddrItemT2o extends CpfItem {

    public static final int TYPE_ID = 0x8001;
    private final SockAddr sockAddr;

    public SockAddrItemT2o(SockAddr sockAddr) {
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

        SockAddrItemT2o that = (SockAddrItemT2o) o;

        if (!sockAddr.equals(that.sockAddr)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sockAddr.hashCode();
    }

    private static final int ITEM_LENGTH = 16;

    public static ByteBuf encode(SockAddrItemT2o item, ByteBuf buffer) {
        buffer.writeShort(TYPE_ID);
        buffer.writeShort(ITEM_LENGTH);

        return SockAddr.encode(item.getSockAddr(), buffer);
    }

    public static SockAddrItemT2o decode(ByteBuf buffer) {
        int typeId = buffer.readUnsignedShort();
        int length = buffer.readUnsignedShort();

        assert (typeId == TYPE_ID);
        assert (length == ITEM_LENGTH);

        SockAddr sockAddr = SockAddr.decode(buffer);

        return new SockAddrItemT2o(sockAddr);
    }

}
