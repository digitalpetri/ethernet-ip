package com.digitalpetri.enip.cpf;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;

public final class CpfPacket {

    private final CpfItem[] items;

    public CpfPacket(CpfItem... items) {
        this.items = items;
    }

    public CpfItem[] getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CpfPacket cpfPacket = (CpfPacket) o;

        return Arrays.equals(items, cpfPacket.items);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(items);
    }

    public static ByteBuf encode(CpfPacket packet, ByteBuf buffer) {
        buffer.writeShort(packet.getItems().length);

        for (CpfItem item : packet.getItems()) {
            CpfItem.encode(item, buffer);
        }

        return buffer;
    }

    public static CpfPacket decode(ByteBuf buffer) {
        int itemCount = buffer.readUnsignedShort();
        CpfItem[] items = new CpfItem[itemCount];

        for (int i = 0; i < itemCount; i++) {
            items[i] = CpfItem.decode(buffer);
        }

        return new CpfPacket(items);
    }
}
