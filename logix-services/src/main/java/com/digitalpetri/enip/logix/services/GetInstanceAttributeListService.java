package com.digitalpetri.enip.logix.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.DataSegment;
import com.digitalpetri.enip.cip.epath.EPath;
import com.digitalpetri.enip.cip.epath.LogicalSegment;
import com.digitalpetri.enip.cip.services.CipService;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class GetInstanceAttributeListService<T> implements CipService<List<T>> {

    public static final int SERVICE_CODE = 0x55;

    private final List<T> instances = new CopyOnWriteArrayList<>();

    private volatile int instanceId = 0;
    private volatile int lastInstanceId = 0;

    private final String program;
    private final int classId;
    private final int[] attributes;
    private final AttributesDecoder<T> attributesDecoder;

    public GetInstanceAttributeListService(
        @Nullable String program,
        int classId,
        @Nonnull int[] attributes,
        AttributesDecoder<T> attributesDecoder) {

        this.program = program;
        this.classId = classId;
        this.attributes = attributes;
        this.attributesDecoder = attributesDecoder;
    }

    @Override
    public void encodeRequest(ByteBuf buffer) {
        EPath.PaddedEPath requestPath = Optional.ofNullable(program)
            .map(p ->
                new EPath.PaddedEPath(
                    new DataSegment.AnsiDataSegment(p),
                    new LogicalSegment.ClassId(classId),
                    new LogicalSegment.InstanceId(instanceId)))
            .orElse(
                new EPath.PaddedEPath(
                    new LogicalSegment.ClassId(classId),
                    new LogicalSegment.InstanceId(instanceId)));

        MessageRouterRequest request = new MessageRouterRequest(
            SERVICE_CODE,
            requestPath,
            b -> {
                b.writeShort(attributes.length);
                for (int attr : attributes) {
                    b.writeShort(attr);
                }
            }
        );

        MessageRouterRequest.encode(request, buffer);
    }

    @Override
    public List<T> decodeResponse(ByteBuf buffer) throws CipResponseException, PartialResponseException {
        MessageRouterResponse response = MessageRouterResponse.decode(buffer);

        int status = response.getGeneralStatus();
        ByteBuf data = response.getData();

        try {
            if (status == 0x00 || status == 0x06) {
                instances.addAll(decode(data));

                if (status == 0x00) {
                    return new ArrayList<>(instances);
                } else {
                    instanceId = lastInstanceId + 1;

                    throw PartialResponseException.INSTANCE;
                }
            } else {
                throw new CipResponseException(status, response.getAdditionalStatus());
            }
        } finally {
            ReferenceCountUtil.release(data);
        }
    }

    private List<T> decode(ByteBuf buffer) {
        List<T> list = new ArrayList<>();

        while (buffer.isReadable()) {
            // reply data includes instanceId + requested attributes
            lastInstanceId = buffer.readInt();

            list.add(attributesDecoder.decode(lastInstanceId, buffer));
        }

        return list;
    }

    @FunctionalInterface
    interface AttributesDecoder<T> {

        /**
         * Decode the requested attributes from {@code buffer}.
         * <p>
         * The instance id has already been decoded and provided.
         *
         * @param instanceId the instanceId.
         * @param buffer     the buffer containing the requested attributes.
         * @return the decoded instance and attributes.
         */
        T decode(int instanceId, ByteBuf buffer);

    }

}
