package com.digitalpetri.enip.commands;

import io.netty.buffer.ByteBuf;

public final class UnRegisterSession extends Command {

    public UnRegisterSession() {
        super(CommandCode.UnRegisterSession);
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

    public static ByteBuf encode(UnRegisterSession command, ByteBuf buffer) {
        return buffer;
    }

    public static UnRegisterSession decode(ByteBuf buffer) {
        return new UnRegisterSession();
    }

}
