package com.digitalpetri.enip.cip.structs;

import io.netty.buffer.ByteBuf;

public class AttributeRequest {

    private final int id;
    private final ByteBuf data;

    public AttributeRequest(int id, ByteBuf data) {
        this.id = id;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public ByteBuf getData() {
        return data;
    }

}
