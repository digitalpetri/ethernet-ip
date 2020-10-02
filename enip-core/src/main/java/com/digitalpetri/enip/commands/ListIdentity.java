package com.digitalpetri.enip.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.digitalpetri.enip.cpf.CipIdentityItem;
import com.digitalpetri.enip.cpf.CipSecurityItem;
import com.digitalpetri.enip.cpf.CpfItem;
import com.digitalpetri.enip.cpf.CpfPacket;
import io.netty.buffer.ByteBuf;

public final class ListIdentity extends Command {

    public static final CommandCode COMMAND_CODE = CommandCode.ListIdentity;

    private final CipIdentityItem identityItem;
    private final CipSecurityItem securityItem;

    public ListIdentity() {
        this(null);
    }

    public ListIdentity(CipIdentityItem identityItem) {
        this(identityItem, null);
    }

    public ListIdentity(CipIdentityItem identityItem, CipSecurityItem securityItem) {
        super(COMMAND_CODE);

        this.identityItem = identityItem;
        this.securityItem = securityItem;
    }

    /**
     * @deprecated use {@link #getIdentityItem()}
     */
    @Deprecated
    public Optional<CipIdentityItem> getIdentity() {
        return Optional.ofNullable(identityItem);
    }

    /**
     * @return an {@link Optional} containing the {@link CipIdentityItem}, if present.
     */
    public Optional<CipIdentityItem> getIdentityItem() {
        return Optional.ofNullable(identityItem);
    }

    /**
     * @return an {@link Optional} containing the {@link CipSecurityItem}, if present.
     */
    public Optional<CipSecurityItem> getSecurityItem() {
        return Optional.ofNullable(securityItem);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListIdentity that = (ListIdentity) o;
        return Objects.equals(identityItem, that.identityItem) &&
            Objects.equals(securityItem, that.securityItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identityItem, securityItem);
    }

    public static ByteBuf encode(ListIdentity command, ByteBuf buffer) {
        List<CpfItem> items = new ArrayList<>();
        command.getIdentityItem().ifPresent(items::add);
        command.getSecurityItem().ifPresent(items::add);

        if (items.size() > 0) {
            CpfItem[] itemArray = items.toArray(new CpfItem[0]);
            CpfPacket.encode(new CpfPacket(itemArray), buffer);
        }

        return buffer;
    }

    public static ListIdentity decode(ByteBuf buffer) {
        if (buffer.readableBytes() > 0) {
            CpfPacket packet = CpfPacket.decode(buffer);
            CpfItem[] items = packet.getItems();

            CipIdentityItem identityItem;
            CipSecurityItem securityItem = null;

            if (items.length >= 1) {
                assert items[0] instanceof CipIdentityItem;
                identityItem = (CipIdentityItem) items[0];

                if (items.length >= 2 && items[1] instanceof CipSecurityItem) {
                    securityItem = (CipSecurityItem) items[1];
                }

                return new ListIdentity(identityItem, securityItem);
            } else {
                return new ListIdentity();
            }
        }

        return new ListIdentity();
    }

}
