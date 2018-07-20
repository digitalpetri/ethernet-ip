package com.digitalpetri.enip.pccc.services;

import com.digitalpetri.enip.pccc.PcccResponseException;

import io.netty.buffer.ByteBuf;

public interface PcccService<T> {
	void encodeRequest(ByteBuf buffer);

	T decodeResponse(ByteBuf buffer) throws PcccResponseException, PartialResponseException;

	public static final class PartialResponseException extends Exception {
		private static final long serialVersionUID = 3767099761970910476L;

		public static final PartialResponseException INSTANCE = new PartialResponseException();
	}
}