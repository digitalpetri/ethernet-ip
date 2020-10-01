package com.digitalpetri.enip.cpf;

import java.util.Objects;

import io.netty.buffer.ByteBuf;

public class CipSecurityItem extends CpfItem {

    public static final int TYPE_ID = 0x86;

    private final int securityProfiles;
    private final int cipSecurityState;
    private final int enipSecurityState;
    private final int ianaPortState;

    public CipSecurityItem(int securityProfiles, int cipSecurityState, int enipSecurityState, int ianaPortState) {
        super(TYPE_ID);

        this.securityProfiles = securityProfiles;
        this.cipSecurityState = cipSecurityState;
        this.enipSecurityState = enipSecurityState;
        this.ianaPortState = ianaPortState;
    }

    public int getSecurityProfiles() {
        return securityProfiles;
    }

    public int getCipSecurityState() {
        return cipSecurityState;
    }

    public int getEnipSecurityState() {
        return enipSecurityState;
    }

    public int getIanaPortState() {
        return ianaPortState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CipSecurityItem that = (CipSecurityItem) o;
        return securityProfiles == that.securityProfiles &&
            cipSecurityState == that.cipSecurityState &&
            enipSecurityState == that.enipSecurityState &&
            ianaPortState == that.ianaPortState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(securityProfiles, cipSecurityState, enipSecurityState, ianaPortState);
    }

    public static ByteBuf encode(CipSecurityItem item, ByteBuf buffer) {
        buffer.writeShort(TYPE_ID);

        // Length placeholder...
        int lengthStartIndex = buffer.writerIndex();
        buffer.writeShort(0);

        // Encode the item...
        int itemStartIndex = buffer.writerIndex();
        buffer.writeShort(item.getSecurityProfiles());
        buffer.writeByte(item.getCipSecurityState() & 0xFF);
        buffer.writeByte(item.getEnipSecurityState() & 0xFF);
        buffer.writeByte(item.getIanaPortState() & 0xFF);

        // Go back and update the length.
        int bytesWritten = buffer.writerIndex() - itemStartIndex;
        buffer.markWriterIndex();
        buffer.writerIndex(lengthStartIndex);
        buffer.writeShort(bytesWritten);
        buffer.resetWriterIndex();

        return buffer;
    }

    public static CipSecurityItem decode(ByteBuf buffer) {
        int typeId = buffer.readUnsignedShort();
        assert typeId == TYPE_ID;

        buffer.skipBytes(2); // length

        return new CipSecurityItem(
            buffer.readShort(),
            buffer.readUnsignedByte(),
            buffer.readUnsignedByte(),
            buffer.readUnsignedByte()
        );
    }

}
