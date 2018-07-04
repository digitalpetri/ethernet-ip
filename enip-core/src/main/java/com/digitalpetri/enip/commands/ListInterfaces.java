package com.digitalpetri.enip.commands;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;

/**
 * The optional List Interfaces command shall be used by a connection originator to identify non-CIP communication
 * interfaces associated with the target. A session need not be established to send this command.
 */
public final class ListInterfaces extends Command {

    private final InterfaceInformation[] interfaces;

    public ListInterfaces(InterfaceInformation... interfaces) {
        super(CommandCode.ListInterfaces);

        this.interfaces = interfaces;
    }

    public InterfaceInformation[] getInterfaces() {
        return interfaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListInterfaces that = (ListInterfaces) o;

        return Arrays.equals(interfaces, that.interfaces);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(interfaces);
    }

    public static ByteBuf encode(ListInterfaces command, ByteBuf buffer) {
        if (command.getInterfaces().length != 0) {
            buffer.writeShort(command.getInterfaces().length);

            for (InterfaceInformation interfaceInformation : command.getInterfaces()) {
                InterfaceInformation.encode(interfaceInformation, buffer);
            }
        }

        return buffer;
    }

    public static ListInterfaces decode(ByteBuf buffer) {
        int itemCount = buffer.readableBytes() >= 2 ? buffer.readUnsignedShort() : 0;

        InterfaceInformation[] interfaces = new InterfaceInformation[itemCount];

        for (int i = 0; i < itemCount; i++) {
            interfaces[i] = InterfaceInformation.decode(buffer);
        }

        return new ListInterfaces(interfaces);
    }

    public static class InterfaceInformation {

        private final int itemId;
        private final byte[] data;

        public InterfaceInformation(int itemId, byte[] data) {
            this.itemId = itemId;
            this.data = data;
        }

        public int getItemId() {
            return itemId;
        }

        public byte[] getData() {
            return data;
        }

        public static ByteBuf encode(InterfaceInformation interfaceInformation, ByteBuf buffer) {
            buffer.writeShort(interfaceInformation.getItemId());
            buffer.writeShort(interfaceInformation.getData().length);
            buffer.writeBytes(interfaceInformation.getData());

            return buffer;
        }

        public static InterfaceInformation decode(ByteBuf buffer) {
            int itemId = buffer.readUnsignedShort();
            int dataLength = buffer.readUnsignedShort();
            byte[] data = new byte[dataLength];
            buffer.readBytes(data);

            return new InterfaceInformation(itemId, data);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InterfaceInformation that = (InterfaceInformation) o;

            return itemId == that.itemId && Arrays.equals(data, that.data);
        }

        @Override
        public int hashCode() {
            int result = itemId;
            result = 31 * result + Arrays.hashCode(data);
            return result;
        }

    }
}
