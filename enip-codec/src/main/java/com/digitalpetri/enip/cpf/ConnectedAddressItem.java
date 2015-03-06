package com.digitalpetri.enip.cpf;

import io.netty.buffer.ByteBuf;

/**
 * This address item shall be used when the encapsulated protocol is connection-oriented. The data shall contain a
 * connection identifier.
 */
public final class ConnectedAddressItem extends CpfItem {

    public static final int TYPE_ID = 0xA1;

    private final int connectionId;

    /**
     * @param connectionId the connection identifier, exchanged in the Forward Open service of the Connection Manager.
     */
    public ConnectedAddressItem(int connectionId) {
        super(TYPE_ID);

        this.connectionId = connectionId;
    }

    public int getConnectionId() {
        return connectionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectedAddressItem that = (ConnectedAddressItem) o;

        return connectionId == that.connectionId;
    }

    @Override
    public int hashCode() {
        return connectionId;
    }

    private static final int ITEM_LENGTH = 4;

    public static ByteBuf encode(ConnectedAddressItem item, ByteBuf buffer) {
        buffer.writeShort(item.getTypeId());
        buffer.writeShort(ITEM_LENGTH);
        buffer.writeInt(item.getConnectionId());

        return buffer;
    }

    public static ConnectedAddressItem decode(ByteBuf buffer) {
        int typeId = buffer.readUnsignedShort();
        int length = buffer.readUnsignedShort();
        int connectionId = buffer.readInt();

        assert (typeId == TYPE_ID);
        assert (length == ITEM_LENGTH);

        return new ConnectedAddressItem(connectionId);
    }

}
