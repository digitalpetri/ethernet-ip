package com.digitalpetri.enip.cip.structs;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;

public class MessageRouterResponse {

    private final int serviceCode;
    private final int generalStatus;
    private final int[] additionalStatus;

    @Nullable
    private final ByteBuf data;

    public MessageRouterResponse(int serviceCode,
                                 int generalStatus,
                                 int[] additionalStatus,
                                 @Nullable ByteBuf data) {

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

    @Nullable
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
                buffer.readSlice(buffer.readableBytes()).retain() : null;

        return new MessageRouterResponse(serviceCode, generalStatus, additionalStatus, data);
    }

}
