package com.digitalpetri.enip.cpf;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;

public final class CipIdentityItem extends CpfItem {

    public static final int TYPE_ID = 0x0C;

    private final int protocolVersion;
    private final SockAddr socketAddress;
    private final int vendorId;
    private final int deviceType;
    private final int productCode;
    private final short revisionMajor;
    private final short revisionMinor;
    private final short status;
    private final long serialNumber;
    private final String productName;
    private final short state;

    /**
     * @param protocolVersion encapsulation protocol version supported (also returned with
     *                        {@link com.digitalpetri.enip.commands.RegisterSession} reply).
     * @param socketAddress   {@link SockAddr} structure.
     * @param vendorId        device manufacturers vendor ID.
     * @param deviceType      device type of product.
     * @param productCode     product code assigned with respect to device type.
     * @param revisionMajor   device major revision.
     * @param revisionMinor   device minor revision.
     * @param status          current status of device.
     * @param serialNumber    serial number of device.
     * @param productName     human readable description of device.
     * @param state           current state of device.
     */
    public CipIdentityItem(int protocolVersion,
                           SockAddr socketAddress,
                           int vendorId,
                           int deviceType,
                           int productCode,
                           short revisionMajor,
                           short revisionMinor,
                           short status,
                           long serialNumber,
                           String productName,
                           short state) {

        super(TYPE_ID);

        this.protocolVersion = protocolVersion;
        this.socketAddress = socketAddress;
        this.vendorId = vendorId;
        this.deviceType = deviceType;
        this.productCode = productCode;
        this.revisionMajor = revisionMajor;
        this.revisionMinor = revisionMinor;
        this.status = status;
        this.serialNumber = serialNumber;
        this.productName = productName;
        this.state = state;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public SockAddr getSocketAddress() {
        return socketAddress;
    }

    public int getVendorId() {
        return vendorId;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public int getProductCode() {
        return productCode;
    }

    public short getRevisionMajor() {
        return revisionMajor;
    }

    public short getRevisionMinor() {
        return revisionMinor;
    }

    public short getStatus() {
        return status;
    }

    public long getSerialNumber() {
        return serialNumber;
    }

    public String getProductName() {
        return productName;
    }

    public short getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CipIdentityItem that = (CipIdentityItem) o;

        return deviceType == that.deviceType &&
            productCode == that.productCode &&
            protocolVersion == that.protocolVersion &&
            revisionMajor == that.revisionMajor &&
            revisionMinor == that.revisionMinor &&
            serialNumber == that.serialNumber &&
            state == that.state &&
            status == that.status &&
            vendorId == that.vendorId &&
            productName.equals(that.productName) &&
            socketAddress.equals(that.socketAddress);
    }

    @Override
    public int hashCode() {
        int result = protocolVersion;
        result = 31 * result + socketAddress.hashCode();
        result = 31 * result + vendorId;
        result = 31 * result + deviceType;
        result = 31 * result + productCode;
        result = 31 * result + (int) revisionMajor;
        result = 31 * result + (int) revisionMinor;
        result = 31 * result + (int) status;
        result = 31 * result + (int) (serialNumber ^ (serialNumber >>> 32));
        result = 31 * result + productName.hashCode();
        result = 31 * result + (int) state;
        return result;
    }

    public static ByteBuf encode(CipIdentityItem item, ByteBuf buffer) {
        buffer.writeShort(TYPE_ID);

        // Length placeholder...
        int lengthStartIndex = buffer.writerIndex();
        buffer.writeShort(0);

        // Encode the item...
        int itemStartIndex = buffer.writerIndex();
        buffer.writeShort(item.getProtocolVersion());
        SockAddr.encode(item.getSocketAddress(), buffer);
        buffer.writeShort(item.getVendorId());
        buffer.writeShort(item.getDeviceType());
        buffer.writeShort(item.getProductCode());
        buffer.writeByte(item.getRevisionMajor());
        buffer.writeByte(item.getRevisionMinor());
        buffer.writeShort(item.getStatus());
        buffer.writeInt((int) item.getSerialNumber());
        writeString(item.getProductName(), buffer);
        buffer.writeByte(item.getState());

        // Go back and update the length.
        int bytesWritten = buffer.writerIndex() - itemStartIndex;
        buffer.markWriterIndex();
        buffer.writerIndex(lengthStartIndex);
        buffer.writeShort(bytesWritten);
        buffer.resetWriterIndex();

        return buffer;
    }

    public static CipIdentityItem decode(ByteBuf buffer) {
        int typeId = buffer.readUnsignedShort();
        buffer.skipBytes(2); // length

        assert (typeId == TYPE_ID);

        return new CipIdentityItem(
            buffer.readUnsignedShort(),
            SockAddr.decode(buffer),
            buffer.readUnsignedShort(),
            buffer.readUnsignedShort(),
            buffer.readUnsignedShort(),
            buffer.readUnsignedByte(),
            buffer.readUnsignedByte(),
            buffer.readShort(),
            buffer.readUnsignedInt(),
            readString(buffer),
            buffer.readUnsignedByte()
        );
    }

    private static String readString(ByteBuf buffer) {
        int length = Math.min(buffer.readUnsignedByte(), 255);
        byte[] bs = new byte[length];
        buffer.readBytes(bs);

        return new String(bs, Charset.forName("US-ASCII"));
    }

    private static void writeString(String s, ByteBuf buffer) {
        int length = Math.min(s.length(), 255);
        buffer.writeByte(length);
        buffer.writeBytes(s.getBytes(Charset.forName("US-ASCII")), 0, length);
    }

}
