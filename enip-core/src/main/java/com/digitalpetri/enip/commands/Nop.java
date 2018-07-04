package com.digitalpetri.enip.commands;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;

public final class Nop extends Command {

    private final byte[] data;

    public Nop() {
        this(new byte[0]);
    }

    public Nop(byte[] data) {
        super(CommandCode.Nop);

        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Nop nop = (Nop) o;

        return Arrays.equals(data, nop.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public static ByteBuf encode(Nop command, ByteBuf buffer) {
        buffer.writeBytes(command.getData());

        return buffer;
    }

    public static Nop decode(ByteBuf buffer) {
        int size = Math.min(buffer.readableBytes(), 65511);
        byte[] data = new byte[size];
        buffer.readBytes(data);

        return new Nop(data);
    }

}
