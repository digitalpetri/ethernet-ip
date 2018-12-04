package com.digitalpetri.enip.logix.services;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.services.CipService;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

public class ReadTagFragmentedService implements CipService<ByteBuf> {

    public static final int SERVICE_CODE = 0x52;

    private final Consumer<ByteBuf> dataEncoder = this::encode;

    private final List<ByteBuf> buffers = Collections.synchronizedList(new ArrayList<>());
    private volatile int offset = 0;

    private final PaddedEPath requestPath;
    private final int elementCount;

    public ReadTagFragmentedService(PaddedEPath requestPath) {
        this(requestPath, 1);
    }

    public ReadTagFragmentedService(PaddedEPath requestPath, int elementCount) {
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

    private void encode(ByteBuf buffer) {
        buffer.writeShort(elementCount);
        buffer.writeInt(offset);
    }

    @Override
    public ByteBuf decodeResponse(ByteBuf buffer) throws CipResponseException, PartialResponseException {
        MessageRouterResponse response = MessageRouterResponse.decode(buffer);

        int status = response.getGeneralStatus();
        ByteBuf data = response.getData();

        try {
            if (status == 0x00 || status == 0x06) {
                if (status == 0x06 && data.readableBytes() == 0) {
                    throw PartialResponseException.INSTANCE;
                }

                boolean structured = data.getShort(data.readerIndex()) == 0x02A0;
                ByteBuf header = structured ? data.readSlice(4) : data.readSlice(2);
                ByteBuf fragment = data.slice().retain();

                buffers.add(fragment);
                offset += fragment.readableBytes();

                if (status == 0x00) {
                    synchronized (buffers) {
                        ByteBuf composite = Unpooled.compositeBuffer()
                            .addComponent(header.retain())
                            .addComponents(buffers)
                            .writerIndex(header.readableBytes() + offset)
                            .order(ByteOrder.LITTLE_ENDIAN);

                        // Clean up so this service can be re-used...
                        buffers.clear();
                        offset = 0;

                        return composite;
                    }
                } else {
                    throw PartialResponseException.INSTANCE;
                }
            } else {
                throw new CipResponseException(status, response.getAdditionalStatus());
            }
        } finally {
            ReferenceCountUtil.release(data);
        }
    }

}
