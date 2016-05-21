package com.digitalpetri.enip.cip.structs;

import java.util.function.Consumer;

import com.digitalpetri.enip.cip.epath.EPath;
import io.netty.buffer.ByteBuf;

public class MessageRouterRequest {

    private final int serviceCode;
    private final EPath.PaddedEPath requestPath;
    private final Consumer<ByteBuf> dataEncoder;

    public MessageRouterRequest(int serviceCode, EPath.PaddedEPath requestPath, ByteBuf requestData) {
        this.serviceCode = serviceCode;
        this.requestPath = requestPath;
        this.dataEncoder = (buffer) -> buffer.writeBytes(requestData);
    }

    public MessageRouterRequest(int serviceCode, EPath.PaddedEPath requestPath, Consumer<ByteBuf> dataEncoder) {
        this.serviceCode = serviceCode;
        this.requestPath = requestPath;
        this.dataEncoder = dataEncoder;
    }

    public static void encode(MessageRouterRequest request, ByteBuf buffer) {
        buffer.writeByte(request.serviceCode);
        EPath.encode(request.requestPath, buffer);
        request.dataEncoder.accept(buffer);
    }

}
