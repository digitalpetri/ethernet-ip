package com.digitalpetri.enip.commands;

import com.digitalpetri.enip.cpf.CpfPacket;
import io.netty.buffer.ByteBuf;

public final class SendUnitData extends Command {

    public static final long DEFAULT_INTERFACE_HANDLE = 0;
    public static final int DEFAULT_TIMEOUT = 0;

    private final long interfaceHandle;
    private final int timeout;
    private final CpfPacket packet;

    public SendUnitData(CpfPacket packet) {
        this(DEFAULT_INTERFACE_HANDLE, DEFAULT_TIMEOUT, packet);
    }

    public SendUnitData(long interfaceHandle, int timeout, CpfPacket packet) {
        super(CommandCode.SendUnitData);

        this.interfaceHandle = interfaceHandle;
        this.timeout = timeout;
        this.packet = packet;
    }

    public long getInterfaceHandle() {
        return interfaceHandle;
    }

    public int getTimeout() {
        return timeout;
    }

    public CpfPacket getPacket() {
        return packet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SendUnitData that = (SendUnitData) o;

        return interfaceHandle == that.interfaceHandle &&
            timeout == that.timeout &&
            packet.equals(that.packet);
    }

    @Override
    public int hashCode() {
        int result = (int) (interfaceHandle ^ (interfaceHandle >>> 32));
        result = 31 * result + timeout;
        result = 31 * result + packet.hashCode();
        return result;
    }

    public static ByteBuf encode(SendUnitData command, ByteBuf buffer) {
        buffer.writeInt((int) command.getInterfaceHandle());
        buffer.writeShort(command.getTimeout());

        CpfPacket.encode(command.getPacket(), buffer);

        return buffer;
    }

    public static SendUnitData decode(ByteBuf buffer) {
        return new SendUnitData(
            buffer.readUnsignedInt(),
            buffer.readUnsignedShort(),
            CpfPacket.decode(buffer)
        );
    }

}
