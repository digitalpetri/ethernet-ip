package com.digitalpetri.enip.logix.services;

import java.util.function.Consumer;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.services.CipService;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class ReadTagService implements CipService<ByteBuf> {

    public static final int SERVICE_CODE = 0x4C;

    private final Consumer<ByteBuf> dataEncoder = this::encode;

    private final PaddedEPath requestPath;
    private final int elementCount;

    /**
     * Create a ReadTagService requesting 1 element at {@code requestPath}.
     *
     * @param requestPath the path to the tag to read.
     */
    public ReadTagService(PaddedEPath requestPath) {
        this(requestPath, 1);
    }

    /**
     * Create a ReadTagService requesting {@code elementCount} elements at {@code requestPath}.
     *
     * @param requestPath  the path to the tag to read.
     * @param elementCount the number of elements to request.
     */
    public ReadTagService(PaddedEPath requestPath, int elementCount) {
        this.requestPath = requestPath;
        this.elementCount = elementCount;
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

    @Override
    public ByteBuf decodeResponse(ByteBuf buffer) throws PartialResponseException, CipResponseException {
        MessageRouterResponse response = MessageRouterResponse.decode(buffer);

        int generalStatus = response.getGeneralStatus();

        try {
            if (generalStatus == 0x00) {
                return decode(response);
            } else {
                throw new CipResponseException(generalStatus, response.getAdditionalStatus());
            }
        } finally {
            ReferenceCountUtil.release(response.getData());
        }
    }

    private void encode(ByteBuf buffer) {
        buffer.writeShort(elementCount);
    }

    private ByteBuf decode(MessageRouterResponse response) {
        return response.getData().retain();
    }

}
