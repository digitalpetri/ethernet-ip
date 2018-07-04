package com.digitalpetri.enip.cip.structs;

import io.netty.buffer.ByteBuf;

public final class AttributeResponse {

    private final int id;
    private final int status;
    private final ByteBuf data;

    public AttributeResponse(int id, int status, ByteBuf data) {
        this.id = id;
        this.status = status;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public ByteBuf getData() {
        return data;
    }

}
