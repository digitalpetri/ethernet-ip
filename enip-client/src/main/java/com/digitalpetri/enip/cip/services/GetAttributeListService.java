package com.digitalpetri.enip.cip.services;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.structs.AttributeResponse;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

public class GetAttributeListService implements CipService<AttributeResponse[]> {

    public static final int SERVICE_CODE = 0x03;

    private final int[] attributeIds;
    private final int[] attributeSizes;
    private final PaddedEPath requestPath;

    public GetAttributeListService(int[] attributeIds, int[] attributeSizes, PaddedEPath requestPath) {
        this.attributeIds = attributeIds;
        this.attributeSizes = attributeSizes;
        this.requestPath = requestPath;
    }

    @Override
    public void encodeRequest(ByteBuf buffer) {
        MessageRouterRequest request = new MessageRouterRequest(
                SERVICE_CODE,
                requestPath,
                this::encode
        );

        MessageRouterRequest.encode(request, buffer);
    }

    @Override
    public AttributeResponse[] decodeResponse(ByteBuf buffer) throws CipResponseException, PartialResponseException {
        MessageRouterResponse response = MessageRouterResponse.decode(buffer);

        if (response.getGeneralStatus() == 0x00) {
            try {
                return decode(response.getData());
            } finally {
                ReferenceCountUtil.release(response.getData());
            }
        } else {
            throw new CipResponseException(response.getGeneralStatus(), response.getAdditionalStatus());
        }
    }

    private void encode(ByteBuf buffer) {
        buffer.writeShort(attributeIds.length);

        for (int id : attributeIds) {
            buffer.writeShort(id);
        }
    }

    private AttributeResponse[] decode(ByteBuf buffer) {
        int count = buffer.readUnsignedShort();

        AttributeResponse[] attributeResponses = new AttributeResponse[count];

        for (int i = 0; i < count; i++) {
            int id = buffer.readUnsignedShort();
            int status = buffer.readUnsignedShort();
            ByteBuf data = status == 0x00 ?
                    buffer.readSlice(attributeSizes[i]).copy() :
                    Unpooled.EMPTY_BUFFER;

            attributeResponses[i] = new AttributeResponse(id, status, data);
        }

        return attributeResponses;
    }

}
