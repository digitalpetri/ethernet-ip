package com.digitalpetri.enip.logix.services;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.services.CipService;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class WriteTagService implements CipService<Void> {

    public static final int SERVICE_CODE = 0x4D;

    private final PaddedEPath requestPath;
    private final boolean structured;
    private final int tagType;
    private final int elementCount;
    private final ByteBuf data;

    public WriteTagService(PaddedEPath requestPath, boolean structured, int tagType, ByteBuf data) {
        this(requestPath, structured, tagType, 1, data);
    }

    public WriteTagService(PaddedEPath requestPath,
                           boolean structured,
                           int tagType,
                           int elementCount,
                           ByteBuf data) {

        this.requestPath = requestPath;
        this.structured = structured;
        this.tagType = tagType;
        this.elementCount = elementCount;
        this.data = data;
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
    public Void decodeResponse(ByteBuf buffer) throws CipResponseException, PartialResponseException {
        MessageRouterResponse response = MessageRouterResponse.decode(buffer);

        int generalStatus = response.getGeneralStatus();

        try {
            if (generalStatus == 0x00) {
                return null;
            } else {
                throw new CipResponseException(generalStatus, response.getAdditionalStatus());
            }
        } finally {
            ReferenceCountUtil.release(response.getData());
        }
    }

    private void encode(ByteBuf buffer) {
        if (structured) {
            buffer.writeByte(0xA0).writeByte(0x02);
        }

        buffer.writeShort(tagType);
        buffer.writeShort(elementCount);
        buffer.writeBytes(data);
    }

}
