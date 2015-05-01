package com.digitalpetri.enip.cip.services;

import java.util.List;
import java.util.function.BiConsumer;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.epath.LogicalSegment.ClassId;
import com.digitalpetri.enip.cip.epath.LogicalSegment.InstanceId;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.synchronizedList;

public class MultipleServicePacketService implements CipService<Void> {

    public static final int SERVICE_CODE = 0x0A;

    private static final PaddedEPath MESSAGE_ROUTER_PATH = new PaddedEPath(
            new ClassId(0x02),
            new InstanceId(0x01));

    private final List<CipService<?>> services;
    private final List<BiConsumer<?, Throwable>> consumers;

    public MultipleServicePacketService(List<CipService<?>> services, List<BiConsumer<?, Throwable>> consumers) {
        assert (services.size() == consumers.size());

        this.services = synchronizedList(newArrayList(services));
        this.consumers = synchronizedList(newArrayList(consumers));
    }

    @Override
    public void encodeRequest(ByteBuf buffer) {
        MessageRouterRequest request = new MessageRouterRequest(
                SERVICE_CODE,
                MESSAGE_ROUTER_PATH,
                this::encode);

        MessageRouterRequest.encode(request, buffer);
    }

    @Override
    public Void decodeResponse(ByteBuf buffer) throws CipResponseException, PartialResponseException {
        MessageRouterResponse response = MessageRouterResponse.decode(buffer);

        if (response.getGeneralStatus() == 0x00 || response.getGeneralStatus() == 0x1E) {
            try {
                List<Object[]> partials = newArrayList();

                ByteBuf[] serviceData = decode(response.getData());

                for (int i = 0; i < serviceData.length; i++) {
                    CipService<?> service = services.get(i);

                    @SuppressWarnings("unchecked")
                    BiConsumer<Object, Throwable> consumer =
                            (BiConsumer<Object, Throwable>) consumers.get(i);

                    try {
                        consumer.accept(service.decodeResponse(serviceData[i]), null);
                    } catch (PartialResponseException prx) {
                        partials.add(new Object[]{service, consumer});
                    } catch (Throwable t) {
                        consumer.accept(null, t);
                    } finally {
                        ReferenceCountUtil.release(serviceData[i]);
                    }
                }

                if (!partials.isEmpty()) {
                    services.clear();
                    consumers.clear();

                    for (Object[] oa : partials) {
                        CipService<?> service = (CipService<?>) oa[0];

                        @SuppressWarnings("unchecked")
                        BiConsumer<Object, Throwable> consumer =
                                (BiConsumer<Object, Throwable>) oa[1];

                        services.add(service);
                        consumers.add(consumer);
                    }

                    throw PartialResponseException.INSTANCE;
                }

                return null;
            } finally {
                ReferenceCountUtil.release(response.getData());
            }
        } else {
            throw new CipResponseException(response.getGeneralStatus(), response.getAdditionalStatus());
        }
    }

    private void encode(ByteBuf buffer) {
        int serviceCount = services.size();

        buffer.writeShort(serviceCount);

        int[] offsets = new int[serviceCount];
        int offsetsStartIndex = buffer.writerIndex();
        buffer.writeZero(serviceCount * 2);

        for (int i = 0; i < serviceCount; i++) {
            offsets[i] = buffer.writerIndex() - offsetsStartIndex + 2;
            services.get(i).encodeRequest(buffer);
        }

        buffer.markWriterIndex();
        buffer.writerIndex(offsetsStartIndex);
        for (int offset : offsets) {
            buffer.writeShort(offset);
        }
        buffer.resetWriterIndex();
    }

    private ByteBuf[] decode(ByteBuf buffer) {
        int dataStartIndex = buffer.readerIndex();
        int serviceCount = buffer.readUnsignedShort();

        int[] offsets = new int[serviceCount];
        for (int i = 0; i < serviceCount; i++) {
            offsets[i] = buffer.readUnsignedShort();
        }

        ByteBuf[] serviceData = new ByteBuf[serviceCount];
        for (int i = 0; i < serviceCount; i++) {
            int offset = offsets[i];

            int length = (i + 1 < serviceCount) ?
                    offsets[i + 1] - offset :
                    buffer.readableBytes();

            serviceData[i] = buffer.slice(dataStartIndex + offsets[i], length).retain();

            buffer.skipBytes(length);
        }

        return serviceData;
    }

}
