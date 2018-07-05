package com.digitalpetri.enip;

import io.netty.buffer.ByteBuf;

public enum EnipStatus {

    EIP_SUCCESS(0x00),
    INVALID_UNSUPPORTED(0x01),
    INSUFFICIENT_MEMORY(0x02),
    MALFORMED_DATA(0x03),
    INVALID_SESSION_HANDLE(0x64),
    INVALID_LENGTH(0x65),
    UNSUPPORTED_PROTOCOL_VERSION(0x69);

    private final int status;

    EnipStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public static ByteBuf encode(EnipStatus status, ByteBuf buffer) {
        buffer.writeInt(status.getStatus());

        return buffer;
    }

    public static EnipStatus decode(ByteBuf buffer) {
        int status = buffer.readInt();

        switch (status) {
            case 0x00:
                return EIP_SUCCESS;
            case 0x01:
                return INVALID_UNSUPPORTED;
            case 0x02:
                return INSUFFICIENT_MEMORY;
            case 0x03:
                return MALFORMED_DATA;
            case 0x64:
                return INVALID_SESSION_HANDLE;
            case 0x65:
                return INVALID_LENGTH;
            case 0x69:
                return UNSUPPORTED_PROTOCOL_VERSION;
            default:
                throw new RuntimeException(String.format("unrecognized status: 0x%02X", status));
        }
    }

}
