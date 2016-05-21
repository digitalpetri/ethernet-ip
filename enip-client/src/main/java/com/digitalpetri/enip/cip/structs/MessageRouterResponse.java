package com.digitalpetri.enip.cip.structs;

import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MessageRouterResponse {

    private final int serviceCode;
    private final int generalStatus;
    private final int[] additionalStatus;

    private final ByteBuf data;

    public MessageRouterResponse(int serviceCode,
                                 int generalStatus,
                                 int[] additionalStatus,
                                 @Nonnull ByteBuf data) {

        this.serviceCode = serviceCode;
        this.generalStatus = generalStatus;
        this.additionalStatus = additionalStatus;
        this.data = data;
    }

    public int getServiceCode() {
        return serviceCode;
    }

    public int getGeneralStatus() {
        return generalStatus;
    }

    public int[] getAdditionalStatus() {
        return additionalStatus;
    }

    @Nonnull
    public ByteBuf getData() {
        return data;
    }

    public static MessageRouterResponse decode(ByteBuf buffer) {
        int serviceCode = buffer.readUnsignedByte();
        buffer.skipBytes(1); // reserved
        int generalStatus = buffer.readUnsignedByte();

        int count = buffer.readUnsignedByte();
        int[] additionalStatus = new int[count];
        for (int i = 0; i < count; i++) {
            additionalStatus[i] = buffer.readShort();
        }

        ByteBuf data = buffer.isReadable() ?
            buffer.readSlice(buffer.readableBytes()).retain() : Unpooled.EMPTY_BUFFER;

        return new MessageRouterResponse(serviceCode, generalStatus, additionalStatus, data);
    }

}
