package com.digitalpetri.enip.commands;

import io.netty.buffer.ByteBuf;

public final class RegisterSession extends Command {

    public static final int DEFAULT_PROTOCOL_VERSION = 1;
    public static final int DEFAULT_OPTION_FLAGS = 0;

    private final int protocolVersion;
    private final int optionFlags;

    public RegisterSession() {
        this(DEFAULT_PROTOCOL_VERSION, DEFAULT_OPTION_FLAGS);
    }

    public RegisterSession(int protocolVersion, int optionFlags) {
        super(CommandCode.RegisterSession);

        this.protocolVersion = protocolVersion;
        this.optionFlags = optionFlags;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public int getOptionFlags() {
        return optionFlags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisterSession that = (RegisterSession) o;

        return optionFlags == that.optionFlags && protocolVersion == that.protocolVersion;
    }

    @Override
    public int hashCode() {
        int result = protocolVersion;
        result = 31 * result + optionFlags;
        return result;
    }

    public static ByteBuf encode(RegisterSession command, ByteBuf buffer) {
        buffer.writeShort(command.getProtocolVersion());
        buffer.writeShort(command.getOptionFlags());

        return buffer;
    }

    public static RegisterSession decode(ByteBuf buffer) {
        return new RegisterSession(
            buffer.readUnsignedShort(),
            buffer.readUnsignedShort()
        );
    }

}
