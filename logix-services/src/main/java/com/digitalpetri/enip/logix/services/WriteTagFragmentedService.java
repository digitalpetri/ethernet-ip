package com.digitalpetri.enip.logix.services;

import java.util.function.Consumer;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.services.CipService;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

public class WriteTagFragmentedService implements CipService<ByteBuf> {

    public static final int SERVICE_CODE = 0x53;

    private final Consumer<ByteBuf> dataEncoder = this::encode;

    private final PaddedEPath requestPath;
    private final int elementCount;
    private final boolean structured;
    private final int tagType;
    private final ByteBuf data;
    private final int offset;

    /**
     * @param requestPath  {@link PaddedEPath Path} of tag
     * @param structured   True if tag is structured
     * @param tagType      Type of tag
     * @param elementCount Total number of elements being sent. Usually the number of bytes, but
     *                     can vary based on data type.
     * @param offset       Total number of bytes of data transferred in previous requests
     * @param data         {@link ByteBuf Data} to be sent in request. Data should be a slice of original data, starting
     *                     at offset and ending at an appropriate length for the CIP connection size.
     */
    public WriteTagFragmentedService(
        PaddedEPath requestPath,
        boolean structured,
        int tagType,
        int elementCount,
        int offset,
        ByteBuf data
    ) {

        this.requestPath = requestPath;
        this.structured = structured;
        this.tagType = tagType;
        this.elementCount = elementCount;
        this.offset = offset;
        this.data = data;
    }

    @Override
    public void encodeRequest(ByteBuf buffer) {
        MessageRouterRequest request = new MessageRouterRequest(
            SERVICE_CODE,
            requestPath,
            dataEncoder
        );

        MessageRouterRequest.encode(request, buffer);
    }

    private void encode(ByteBuf buffer) {
        if (structured) {
            buffer.writeByte(0xA0).writeByte(0x02);
        }

        buffer.writeShort(tagType);
        buffer.writeShort(elementCount);
        buffer.writeInt(offset);
        buffer.writeBytes(data);
    }

    @Override
    public ByteBuf decodeResponse(ByteBuf buffer) throws CipResponseException {
        MessageRouterResponse response = MessageRouterResponse.decode(buffer);

        int generalStatus = response.getGeneralStatus();

        try {
            if (generalStatus == 0x00) {
                return Unpooled.EMPTY_BUFFER;
            } else {
                throw new CipResponseException(generalStatus, response.getAdditionalStatus());
            }
        } finally {
            ReferenceCountUtil.release(response.getData());
        }
    }

}
