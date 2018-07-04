package com.digitalpetri.enip.cip.services;

import java.util.function.Function;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.structs.AttributeRequest;
import com.digitalpetri.enip.cip.structs.AttributeResponse;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

public class SetAttributeListService implements CipService<AttributeResponse[]> {

    public static final int SERVICE_CODE = 0x04;

    private final PaddedEPath requestPath;
    private final AttributeRequest[] attributeRequests;
    private final Function<Integer, ByteBuf> attributeDataDecoder;

    public SetAttributeListService(PaddedEPath requestPath,
                                   AttributeRequest[] attributeRequests,
                                   Function<Integer, ByteBuf> attributeDataDecoder) {

        this.requestPath = requestPath;
        this.attributeRequests = attributeRequests;
        this.attributeDataDecoder = attributeDataDecoder;
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

        try {
            if (response.getGeneralStatus() == 0x00) {
                return decode(buffer);
            } else {
                throw new CipResponseException(response.getGeneralStatus(), response.getAdditionalStatus());
            }
        } finally {
            ReferenceCountUtil.release(buffer);
        }
    }

    private void encode(ByteBuf buffer) {
        buffer.writeShort(attributeRequests.length);

        for (AttributeRequest request : attributeRequests) {
            buffer.writeShort(request.getId());
            buffer.writeBytes(request.getData());
        }
    }

    private AttributeResponse[] decode(ByteBuf buffer) {
        int count = buffer.readUnsignedShort();

        AttributeResponse[] responses = new AttributeResponse[count];

        for (int i = 0; i < count; i++) {
            int id = buffer.readShort();
            int status = buffer.readShort();

            ByteBuf data = (status == 0x00) ?
                attributeDataDecoder.apply(id) :
                Unpooled.EMPTY_BUFFER;

            responses[i] = new AttributeResponse(id, status, data);
        }

        return responses;
    }

}
