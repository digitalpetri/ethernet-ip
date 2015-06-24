package com.digitalpetri.enip.cip.structs;

import io.netty.buffer.ByteBuf;

public class ForwardCloseResponse {

    private final int connectionSerialNumber;
    private final int originatorVendorId;
    private final long originatorSerialNumber;

    public ForwardCloseResponse(int connectionSerialNumber, int originatorVendorId, long originatorSerialNumber) {
        this.connectionSerialNumber = connectionSerialNumber;
        this.originatorVendorId = originatorVendorId;
        this.originatorSerialNumber = originatorSerialNumber;
    }

    public int getConnectionSerialNumber() {
        return connectionSerialNumber;
    }

    public int getOriginatorVendorId() {
        return originatorVendorId;
    }

    public long getOriginatorSerialNumber() {
        return originatorSerialNumber;
    }

    public static ForwardCloseResponse decode(ByteBuf buffer) {
        int connectionSerialNumber = buffer.readUnsignedShort();
        int originatorVendorId = buffer.readUnsignedShort();
        long originatorSerialNumber = buffer.readUnsignedInt();

        return new ForwardCloseResponse(connectionSerialNumber, originatorVendorId, originatorSerialNumber);
    }

}
