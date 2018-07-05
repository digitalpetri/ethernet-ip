package com.digitalpetri.enip.cip.services;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class GetAttributeSingleService implements CipService<ByteBuf> {

    public static final int SERVICE_CODE = 0x0E;

    private final PaddedEPath requestPath;

    public GetAttributeSingleService(PaddedEPath requestPath) {
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
    public ByteBuf decodeResponse(ByteBuf buffer) throws CipResponseException, PartialResponseException {
        MessageRouterResponse response = MessageRouterResponse.decode(buffer);

        try {
            if (response.getGeneralStatus() == 0x00) {
                return decode(response.getData());
            } else {
                throw new CipResponseException(response.getGeneralStatus(), response.getAdditionalStatus());
            }
        } finally {
            ReferenceCountUtil.release(response.getData());
        }
    }

    private void encode(ByteBuf buffer) {
    }

    private ByteBuf decode(ByteBuf buffer) {
        return buffer.readSlice(buffer.readableBytes()).retain();
    }

}
