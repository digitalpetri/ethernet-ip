package com.digitalpetri.enip.cip.services;

import com.digitalpetri.enip.cip.CipResponseException;
import io.netty.buffer.ByteBuf;

public interface CipService<T> {

    void encodeRequest(ByteBuf buffer);

    T decodeResponse(ByteBuf buffer) throws CipResponseException, PartialResponseException;

    public static final class PartialResponseException extends Exception {
        public static final PartialResponseException INSTANCE = new PartialResponseException();
    }

}
