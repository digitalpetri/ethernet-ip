package com.digitalpetri.enip.cpf;

import java.nio.ByteOrder;
import java.util.Arrays;

import io.netty.buffer.ByteBuf;

public final class SockAddr {

    private final int sinFamily;
    private final int sinPort;
    private final byte[] sinAddr;
    private final long sinZero;

    public SockAddr(int sinFamily, int sinPort, byte[] sinAddr, long sinZero) {
        this.sinFamily = sinFamily;
        this.sinPort = sinPort;
        this.sinAddr = sinAddr;
        this.sinZero = sinZero;
    }

    public int getSinFamily() {
        return sinFamily;
    }

    public int getSinPort() {
        return sinPort;
    }

    public byte[] getSinAddr() {
        return sinAddr;
    }

    public long getSinZero() {
        return sinZero;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SockAddr sockAddr = (SockAddr) o;

        return sinFamily == sockAddr.sinFamily &&
            sinPort == sockAddr.sinPort &&
            sinZero == sockAddr.sinZero &&
            Arrays.equals(sinAddr, sockAddr.sinAddr);
    }

    @Override
    public int hashCode() {
        int result = sinFamily;
        result = 31 * result + sinPort;
        result = 31 * result + Arrays.hashCode(sinAddr);
        result = 31 * result + (int) (sinZero ^ (sinZero >>> 32));
        return result;
    }

    public static ByteBuf encode(SockAddr sockAddr, ByteBuf buffer) {
        buffer.order(ByteOrder.BIG_ENDIAN).writeShort(sockAddr.getSinFamily());
        buffer.order(ByteOrder.BIG_ENDIAN).writeShort(sockAddr.getSinPort());
        buffer.order(ByteOrder.BIG_ENDIAN).writeBytes(sockAddr.getSinAddr());
        buffer.order(ByteOrder.BIG_ENDIAN).writeLong(sockAddr.getSinZero());

        return buffer;
    }

    public static SockAddr decode(ByteBuf buffer) {
        int sinFamily = buffer.order(ByteOrder.BIG_ENDIAN).readUnsignedShort();
        int sinPort = buffer.order(ByteOrder.BIG_ENDIAN).readUnsignedShort();
        byte[] sinAddr = new byte[4];
        buffer.order(ByteOrder.BIG_ENDIAN).readBytes(sinAddr);
        long sinZero = buffer.order(ByteOrder.BIG_ENDIAN).readLong();

        return new SockAddr(sinFamily, sinPort, sinAddr, sinZero);
    }

}
