package com.digitalpetri.enip.cpf;

import io.netty.buffer.ByteBuf;

public abstract class CpfItem {

    private final int typeId;

    protected CpfItem(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public static ByteBuf encode(CpfItem item, ByteBuf buffer) {
        switch (item.getTypeId()) {
            case CipIdentityItem.TYPE_ID:
                return CipIdentityItem.encode((CipIdentityItem) item, buffer);

            case ConnectedAddressItem.TYPE_ID:
                return ConnectedAddressItem.encode((ConnectedAddressItem) item, buffer);

            case ConnectedDataItemRequest.TYPE_ID:
                return ConnectedDataItemRequest.encode((ConnectedDataItemRequest) item, buffer);

            case NullAddressItem.TYPE_ID:
                return NullAddressItem.encode((NullAddressItem) item, buffer);

            case SequencedAddressItem.TYPE_ID:
                return SequencedAddressItem.encode((SequencedAddressItem) item, buffer);

            case SockAddrItemO2t.TYPE_ID:
                return SockAddrItemO2t.encode((SockAddrItemO2t) item, buffer);

            case SockAddrItemT2o.TYPE_ID:
                return SockAddrItemT2o.encode((SockAddrItemT2o) item, buffer);

            case UnconnectedDataItemRequest.TYPE_ID:
                return UnconnectedDataItemRequest.encode((UnconnectedDataItemRequest) item, buffer);

            case CipSecurityItem.TYPE_ID:
                return CipSecurityItem.encode((CipSecurityItem) item, buffer);

            default:
                throw new RuntimeException(String.format("unhandled item type: 0x%02X", item.getTypeId()));
        }
    }

    public static CpfItem decode(ByteBuf buffer) {
        int typeId = buffer.getUnsignedShort(buffer.readerIndex());

        switch (typeId) {
            case CipIdentityItem.TYPE_ID:
                return CipIdentityItem.decode(buffer);

            case ConnectedAddressItem.TYPE_ID:
                return ConnectedAddressItem.decode(buffer);

            case ConnectedDataItemResponse.TYPE_ID:
                return ConnectedDataItemResponse.decode(buffer);

            case NullAddressItem.TYPE_ID:
                return NullAddressItem.decode(buffer);

            case SequencedAddressItem.TYPE_ID:
                return SequencedAddressItem.decode(buffer);

            case SockAddrItemO2t.TYPE_ID:
                return SockAddrItemO2t.decode(buffer);

            case SockAddrItemT2o.TYPE_ID:
                return SockAddrItemT2o.decode(buffer);

            case UnconnectedDataItemResponse.TYPE_ID:
                return UnconnectedDataItemResponse.decode(buffer);

            case CipSecurityItem.TYPE_ID:
                return CipSecurityItem.decode(buffer);
                
            default:
                throw new RuntimeException(String.format("unhandled item type: 0x%02X", typeId));
        }
    }

}
