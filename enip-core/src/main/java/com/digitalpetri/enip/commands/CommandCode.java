package com.digitalpetri.enip.commands;

import io.netty.buffer.ByteBuf;

public enum CommandCode {

    Nop(0x00),
    ListServices(0x04),
    ListIdentity(0x63),
    ListInterfaces(0x64),
    RegisterSession(0x65),
    UnRegisterSession(0x66),
    SendRRData(0x6F),
    SendUnitData(0x70);

    private final int code;

    CommandCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ByteBuf encode(CommandCode commandCode, ByteBuf buffer) {
        buffer.writeShort(commandCode.getCode());

        return buffer;
    }

    public static CommandCode decode(ByteBuf buffer) {
        int code = buffer.readUnsignedShort();

        switch (code) {
            case 0x00:
                return Nop;
            case 0x04:
                return ListServices;
            case 0x63:
                return ListIdentity;
            case 0x64:
                return ListInterfaces;
            case 0x65:
                return RegisterSession;
            case 0x66:
                return UnRegisterSession;
            case 0x6F:
                return SendRRData;
            case 0x70:
                return SendUnitData;
            default:
                throw new RuntimeException(String.format("unrecognized command code: 0x%02X", code));
        }
    }

}
