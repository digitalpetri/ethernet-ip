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

    /**
     * Get the CIP Security Profiles supported by the device.
     *
     * <pre>
     *  Bit 0       EtherNet/IP Integrity Profile
     *  Bit 1       EtherNet/IP Confidentiality Profile
     *  Bit 2       CIP Authorization Profile
     *  Bit 3       CIP Integrity Profile
     *  Bit 4-15    Reserved
     * </pre>
     *
     * @return the CIP Security Profiles supported by the device.
     */
    public int getSecurityProfiles() {
        return securityProfiles;
    }

    /**
     * Get the current state of the CIP Security Object.
     *
     * <pre>
     *  0   Factory Default Configuration
     *  1   Initial Commissioning In Progress
     *  2   Configured
     *  3   Incomplete Configuration
     * </pre>
     *
     * @return the current state of the CIP Security Object.
     */
    public int getCipSecurityState() {
        return cipSecurityState;
    }

    /**
     * Get the current state of the EtherNet/IP Security Object associated with the IP address
     * where the request was received.
     *
     * <pre>
     *  0   Factory Default Configuration
     *  1   Configuration In Progress
     *  2   Configured
     * </pre>
     *
     * @return the current state of the EtherNet/IP Security Object associated with the IP address
     * where the request was received.
     */
    public int getEnipSecurityState() {
        return enipSecurityState;
    }

    /**
     * Get the current state, open or closed, for all EtherNet/IP related IANA ports Object
     * associated with the IP address where the request was received.
     * <p>
     * 1 (TRUE) shall indicate that the corresponding port is open. If the bit is 0 (FALSE) the
     * port is closed. Reserved bits shall be 0.
     *
     * <pre>
     *  Bit 0    44818/tcp
     *  Bit 1    44818/udp
     *  Bit 2    2222/udp
     *  Bit 3    2221/tcp
     *  Bit 4    2221/udp
     *  Bit 5-7  Reserved
     * </pre>
     *
     * @return the current state, open or closed, for all EtherNet/IP related IANA ports Object
     * associated with the IP address where the request was received.
     */
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
