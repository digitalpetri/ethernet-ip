package com.digitalpetri.enip.logix.services;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.DataSegment.AnsiDataSegment;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.epath.LogicalSegment.ClassId;
import com.digitalpetri.enip.cip.epath.LogicalSegment.InstanceId;
import com.digitalpetri.enip.cip.services.CipService;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import com.digitalpetri.enip.logix.structs.SymbolInstance;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

/**
 * @see GetSymbolInstanceAttributeListService
 * @deprecated use {@link GetSymbolInstanceAttributeListService} instead.
 */
@Deprecated
public class GetInstanceAttributeListService implements CipService<List<SymbolInstance>> {

    public static final int SERVICE_CODE = 0x55;

    private volatile int instanceId = 0;
    private final List<SymbolInstance> symbols = Lists.newCopyOnWriteArrayList();

    private final String program;

    public GetInstanceAttributeListService() {
        this(null);
    }

    public GetInstanceAttributeListService(@Nullable String program) {
        this.program = program;
    }

    @Override
    public void encodeRequest(ByteBuf buffer) {
        PaddedEPath requestPath = Optional.ofNullable(program)
            .map(p ->
                new PaddedEPath(
                    new AnsiDataSegment(p),
                    new ClassId(0x6B),
                    new InstanceId(instanceId)))
            .orElse(
                new PaddedEPath(
                    new ClassId(0x6B),
                    new InstanceId(instanceId)));

        MessageRouterRequest request = new MessageRouterRequest(
            SERVICE_CODE,
            requestPath,
            this::encode
        );

        MessageRouterRequest.encode(request, buffer);
    }

    @Override
    public List<SymbolInstance> decodeResponse(ByteBuf buffer) throws CipResponseException, PartialResponseException {
        MessageRouterResponse response = MessageRouterResponse.decode(buffer);

        int status = response.getGeneralStatus();
        ByteBuf data = response.getData();

        try {
            if (status == 0x00 || status == 0x06) {
                symbols.addAll(decode(data));

                if (status == 0x00) {
                    return Lists.newArrayList(symbols);
                } else {
                    instanceId = symbols.get(symbols.size() - 1).getInstanceId() + 1;

                    throw PartialResponseException.INSTANCE;
                }
            } else {
                throw new CipResponseException(status, response.getAdditionalStatus());
            }
        } finally {
            ReferenceCountUtil.release(data);
        }
    }

    private void encode(ByteBuf buffer) {
        buffer.writeShort(3); // 3 attributes:
        buffer.writeShort(1); // symbol name
        buffer.writeShort(2); // symbol type
        buffer.writeShort(8); // dimensions
    }

    private static final Charset ASCII = Charset.forName("US-ASCII");

    private List<SymbolInstance> decode(ByteBuf buffer) {
        List<SymbolInstance> l = Lists.newArrayList();

        while (buffer.isReadable()) {
            // reply data includes instanceId + requested attributes
            int instanceId = buffer.readInt();

            // attribute 1 - symbol name
            int nameLength = buffer.readUnsignedShort();
            String name = buffer.toString(buffer.readerIndex(), nameLength, ASCII);
            buffer.skipBytes(nameLength);

            // attribute 2 - symbol type
            int type = buffer.readUnsignedShort();

            // attribute 8 - dimensions
            int d1Size = buffer.readInt();
            int d2Size = buffer.readInt();
            int d3Size = buffer.readInt();

            l.add(new SymbolInstance(program, name, instanceId, type, d1Size, d2Size, d3Size));
        }

        return l;
    }

}
