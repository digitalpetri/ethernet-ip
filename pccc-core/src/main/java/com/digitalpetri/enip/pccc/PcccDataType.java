package com.digitalpetri.enip.pccc;

import java.util.Optional;

public enum PcccDataType {
	BIT(1),

	BIT_STRING(2),

	BYTE_STRING(3),

	INT(4),

	TIMER(5),

	COUNTER(6),

	CONTROL(7),

	REAL(8),

	ARRAY(9),

	ADDRESS(15),

	BCD(16);

	private final int code;

	PcccDataType(int code) {
		this.code = code;
	}

	public final int getCode() {
		return code;
	}

	public static Optional<PcccDataType> fromCode(int code) {
		for (PcccDataType dataType : values()) {
			if (dataType.getCode() == code) {
				return Optional.of(dataType);
			}
		}
		return Optional.empty();

	}
}
