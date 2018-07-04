package com.digitalpetri.enip.commands;

import java.util.Optional;

import com.digitalpetri.enip.cpf.CipIdentityItem;
import com.digitalpetri.enip.cpf.CpfItem;
import com.digitalpetri.enip.cpf.CpfPacket;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

public final class ListIdentity extends Command {

    public static final CommandCode COMMAND_CODE = CommandCode.ListIdentity;

    private final Optional<CipIdentityItem> identity;

    public ListIdentity() {
        this(null);
    }

    public ListIdentity(CipIdentityItem identity) {
        super(COMMAND_CODE);

        this.identity = Optional.ofNullable(identity);
    }

    public Optional<CipIdentityItem> getIdentity() {
        return identity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListIdentity that = (ListIdentity) o;

        return identity.equals(that.identity);
    }

    @Override
    public int hashCode() {
        return identity.hashCode();
    }

    public static ByteBuf encode(ListIdentity command, ByteBuf buffer) {
        command.getIdentity().ifPresent(identity -> CpfPacket.encode(new CpfPacket(identity), buffer));

        return buffer;
    }

    public static ListIdentity decode(ByteBuf buffer) {
        if (buffer.readableBytes() > 0) {
            CpfPacket packet = CpfPacket.decode(buffer);
            CpfItem[] items = packet.getItems();

            if (items.length > 0) {
                if (items[0] instanceof CipIdentityItem) {
                    return new ListIdentity((CipIdentityItem) items[0]);
                } else {
                    throw new DecoderException(
                        String.format("expected CipIdentityItem; received %s instead",
                            items[0].getClass().getSimpleName()));
                }
            } else {
                return new ListIdentity();
            }
        }

        return new ListIdentity();
    }

}
