package com.digitalpetri.enip.cpf;

import io.netty.buffer.ByteBuf;

/**
 * This address item shall be used for CIP transport class 0 and class 1 connected data. The data shall contain a
 * connection identifier and a sequence number.
 */
public final class SequencedAddressItem extends CpfItem {

    public static final int TYPE_ID = 0x8002;

    private final long connectionId;
    private final long sequenceNumber;

    /**
     * @param connectionId   connection identifier.
     * @param sequenceNumber sequence number.
     */
    public SequencedAddressItem(long connectionId, long sequenceNumber) {
        super(TYPE_ID);

        this.connectionId = connectionId;
        this.sequenceNumber = sequenceNumber;
    }

    public long getConnectionId() {
        return connectionId;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SequencedAddressItem that = (SequencedAddressItem) o;

        return connectionId == that.connectionId && sequenceNumber == that.sequenceNumber;
    }

    @Override
    public int hashCode() {
        int result = (int) (connectionId ^ (connectionId >>> 32));
        result = 31 * result + (int) (sequenceNumber ^ (sequenceNumber >>> 32));
        return result;
    }

    private static final int ITEM_LENGTH = 8;

    public static ByteBuf encode(SequencedAddressItem item, ByteBuf buffer) {
        buffer.writeShort(TYPE_ID);
        buffer.writeShort(ITEM_LENGTH);
        buffer.writeInt((int) item.getConnectionId());
        buffer.writeInt((int) item.getSequenceNumber());

        return buffer;
    }

    public static SequencedAddressItem decode(ByteBuf buffer) {
        int typeId = buffer.readUnsignedShort();
        int length = buffer.readUnsignedShort();
        long connectionId = buffer.readUnsignedInt();
        long sequenceNumber = buffer.readUnsignedInt();

        assert (typeId == TYPE_ID);
        assert (length == ITEM_LENGTH);

        return new SequencedAddressItem(connectionId, sequenceNumber);
    }

}
