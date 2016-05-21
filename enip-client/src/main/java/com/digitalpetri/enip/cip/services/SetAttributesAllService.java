package com.digitalpetri.enip.cip.services;

import java.util.function.Consumer;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class SetAttributesAllService implements CipService<Void> {

    public static final int SERVICE_CODE = 0x02;

    private final PaddedEPath requestPath;
    private final Consumer<ByteBuf> attributeEncoder;

    public SetAttributesAllService(PaddedEPath requestPath, Consumer<ByteBuf> attributeEncoder) {
        this.requestPath = requestPath;
        this.attributeEncoder = attributeEncoder;
    }

    @Override
    public void encodeRequest(ByteBuf buffer) {
        MessageRouterRequest request = new MessageRouterRequest(
            SERVICE_CODE,
            requestPath,
            attributeEncoder
        );

        MessageRouterRequest.encode(request, buffer);
    }

    @Override
    public Void decodeResponse(ByteBuf buffer) throws CipResponseException, PartialResponseException {
        MessageRouterResponse response = MessageRouterResponse.decode(buffer);

        try {
            if (response.getGeneralStatus() == 0x00) {
                return null;
            } else {
                throw new CipResponseException(response.getGeneralStatus(), response.getAdditionalStatus());
            }
        } finally {
            ReferenceCountUtil.release(response.getData());
        }
    }

}
