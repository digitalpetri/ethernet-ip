package com.digitalpetri.enip.cpf;

import io.netty.buffer.ByteBuf;

/**
 * The null address item shall contain only the type id and the length. The length shall be zero. No data shall follow
 * the length. Since the null address item contains no routing information, it shall be used when the protocol packet
 * itself contains any necessary routing information. The null address item shall be used for Unconnected Messages.
 */
public final class NullAddressItem extends CpfItem {

    public static final int TYPE_ID = 0x00;

    public NullAddressItem() {
        super(TYPE_ID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return !(o == null || getClass() != o.getClass());
    }

    @Override
    public int hashCode() {
        return 0;
    }

    private static final int ITEM_LENGTH = 0;

    public static ByteBuf encode(NullAddressItem item, ByteBuf buffer) {
        buffer.writeShort(TYPE_ID);
        buffer.writeShort(ITEM_LENGTH);

        return buffer;
    }

    public static NullAddressItem decode(ByteBuf buffer) {
        int typeId = buffer.readUnsignedShort();
        int length = buffer.readUnsignedShort();

        assert (typeId == TYPE_ID);
        assert (length == ITEM_LENGTH);

        return new NullAddressItem();
    }

}
