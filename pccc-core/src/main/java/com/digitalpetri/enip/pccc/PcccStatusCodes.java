package com.digitalpetri.enip.pccc;

import com.google.common.collect.ImmutableMap;

public abstract class PcccStatusCodes {
	private PcccStatusCodes() {
	}

	private static final ImmutableMap<Integer, String> STATUS_CODES;

	static {
		STATUS_CODES = ImmutableMap.<Integer, String>builder().put(0x10, "Illegal command or format.")
				.put(0x20, "Host has a problem and will not communicate.")
				.put(0x30, "Remote node host is missing, disconnected, or shut down.")
				.put(0x40, "Host could not complete function due to hardware fault.")
				.put(0x50, "Addressing problem or memory protect rungs.")
				.put(0x60, "Function not allowed due to command protection selection.")
				.put(0x70, "Processor is in Program mode.")
				.put(0x80, "Compatibility mode file missing or communication zone problem.")
				.put(0x90, "Remote node cannot buffer command.").put(0xA0, "Wait ACK (1775-KA buffer full).")
				.put(0xB0, "Remote node problem due to download.").put(0xC0, "Wait ACK (1775-KA buffer full).")
				.put(0xF0, "Error code in the EXT STS byte.").build();
	}

	public static String getName(int statusCode) {
		return STATUS_CODES.getOrDefault(statusCode, "Unknown error response.");
	}
}