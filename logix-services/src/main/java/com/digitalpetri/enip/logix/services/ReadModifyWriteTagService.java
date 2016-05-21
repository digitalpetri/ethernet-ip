package com.digitalpetri.enip.logix.services;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.services.CipService;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class ReadModifyWriteTagService implements CipService<Void> {

    public static final int SERVICE_CODE = 0x4E;

    private final PaddedEPath requestPath;
    private final MaskSize maskSize;
    private final long orMask;
    private final long andMask;

    public ReadModifyWriteTagService(PaddedEPath requestPath, MaskSize maskSize, long orMask, long andMask) {
        this.requestPath = requestPath;
        this.maskSize = maskSize;
        this.orMask = orMask;
        this.andMask = andMask;
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
        switch (maskSize) {
            case ONE_BYTE:
                buffer.writeShort(1);
                buffer.writeByte((int) orMask);
                buffer.writeByte((int) andMask);
                break;
            case TWO_BYTE:
                buffer.writeShort(2);
                buffer.writeShort((int) orMask);
                buffer.writeShort((int) andMask);
                break;
            case FOUR_BYTE:
                buffer.writeShort(4);
                buffer.writeInt((int) orMask);
                buffer.writeInt((int) andMask);
                break;
            case EIGHT_BYTE:
                buffer.writeShort(8);
                buffer.writeLong(orMask);
                buffer.writeLong(andMask);
                break;
        }
    }

    public enum MaskSize {
        ONE_BYTE,
        TWO_BYTE,
        FOUR_BYTE,
        EIGHT_BYTE
    }

}
