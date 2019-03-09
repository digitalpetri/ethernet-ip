package com.digitalpetri.enip.logix.services;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.services.CipService;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import com.digitalpetri.enip.logix.structs.TemplateAttributes;
import com.digitalpetri.enip.logix.structs.TemplateInstance;
import com.digitalpetri.enip.logix.structs.TemplateMember;
import com.digitalpetri.enip.util.IntUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;

public class ReadTemplateService implements CipService<TemplateInstance> {

    public static final int SERVICE_CODE = 0x4C;

    private final List<ByteBuf> buffers = new CopyOnWriteArrayList<>();
    private volatile int totalBytesRead = 0;

    private final PaddedEPath requestPath;
    private final TemplateAttributes attributes;
    private final int symbolType;

    public ReadTemplateService(PaddedEPath requestPath, TemplateAttributes attributes, int symbolType) {
        this.requestPath = requestPath;
        this.attributes = attributes;
        this.symbolType = symbolType;
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
    public TemplateInstance decodeResponse(ByteBuf buffer) throws CipResponseException, PartialResponseException {
        MessageRouterResponse response = MessageRouterResponse.decode(buffer);

        int status = response.getGeneralStatus();

        try {
            if (status == 0x00 || status == 0x06) {
                buffers.add(response.getData().retain());

                totalBytesRead += response.getData().readableBytes();

                if (status == 0x00) {
                    ByteBuf composite = PooledByteBufAllocator.DEFAULT
                        .compositeBuffer(buffers.size())
                        .addComponents(buffers)
                        .writerIndex(totalBytesRead)
                        .order(ByteOrder.LITTLE_ENDIAN);

                    TemplateInstance instance = decode(composite, symbolType);

                    ReferenceCountUtil.release(composite);

                    return instance;
                } else {
                    throw PartialResponseException.INSTANCE;
                }
            } else {
                throw new CipResponseException(status, response.getAdditionalStatus());
            }
        } finally {
            ReferenceCountUtil.release(response.getData());
        }
    }

    private void encode(ByteBuf buffer) {
        int bytesToRead = (attributes.getObjectDefinitionSize() * 4) - 23;
        bytesToRead = roundUp(bytesToRead, 4) + 4;
        bytesToRead -= totalBytesRead;

        buffer.writeInt(totalBytesRead);
        buffer.writeShort(bytesToRead);
    }

    private TemplateInstance decode(ByteBuf buffer, int symbolType) {
        int memberCount = attributes.getMemberCount();

        List<Function<String, TemplateMember>> functions = new ArrayList<>(memberCount);

        for (int i = 0; i < memberCount; i++) {
            int infoWord = buffer.readShort();
            int memberType = buffer.readUnsignedShort();
            int offset = IntUtil.saturatedCast(buffer.readUnsignedInt());

            functions.add((name) -> new TemplateMember(name, infoWord, memberType, offset));
        }

        String templateName = readNullTerminatedString(buffer);

        if (templateName.contains(";n")) {
            templateName = templateName.substring(0, templateName.indexOf(";n"));
        }

        List<TemplateMember> members = new ArrayList<>(memberCount);

        for (int i = 0; i < memberCount; i++) {
            String memberName = readNullTerminatedString(buffer);
            if (memberName.isEmpty()) memberName = "__UnnamedMember" + i;

            TemplateMember member = functions.get(i).apply(memberName);

            members.add(member);
        }

        return new TemplateInstance(templateName, symbolType, attributes, members);
    }

    private static final Charset ASCII = Charset.forName("US-ASCII");

    private String readNullTerminatedString(ByteBuf buffer) {
        int length = buffer.bytesBefore((byte) 0x00);

        if (length == -1) {
            return "";
        } else {
            String s = buffer.toString(buffer.readerIndex(), length, ASCII);
            buffer.skipBytes(length + 1);
            return s;
        }
    }

    /**
     * Round {@code n} up to the nearest multiple {@code m}.
     *
     * @param n the number to round.
     * @param m the multiple to up to.
     * @return {@code n} rounded up to the nearest multiple {@code m}.
     */
    private int roundUp(int n, int m) {
        return ((n + m - 1) / m) * m;
    }

}
