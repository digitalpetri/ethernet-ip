package com.digitalpetri.enip.pccc;

import com.google.common.collect.ImmutableMap;

public abstract class PcccExtendedStatusCodes {
	private PcccExtendedStatusCodes() {
	}

	private static final ImmutableMap<Integer, String> STATUS_CODES;

	static {
		STATUS_CODES = ImmutableMap.<Integer, String>builder().put(0x1, "A field has an illegal value.")
				.put(0x2, "Less levels specified in address than minimum for any address.")
				.put(0x3, "More levels specified in address than system supports.").put(0x4, "Symbol not found.")
				.put(0x5, "Symbol is of improper format.").put(0x6, "Address doesn't point to something usable.")
				.put(0x7, "File is wrong size.")
				.put(0x8, "Cannot complete request, situation has changed since the start of the command.")
				.put(0x9, "Data or file is too large.").put(0xA, "Transaction size plus word address is too large.")
				.put(0xB, "Access denied, improper privilege.")
				.put(0xC, "Condition cannot be generated - resource is not available")
				.put(0xD, "Condition already exists - resource is already available.")
				.put(0xE, "Command cannot be executed.").put(0xF, "Histogram overflow.").put(0x10, "No access.")
				.put(0x11, "Illegal data type.").put(0x12, "Invalid parameter or invalid data.")
				.put(0x13, "Address reference exists to deleted area.")
				.put(0x14, "Command execution failure for unknown reason; possible PLC-3 histogram overflow.")
				.put(0x15, "Data conversion error.")
				.put(0x16, "Scanner not able to communicate with 1771 rack adapter.").put(0x17, "Type mismatch.")
				.put(0x18, "1771 module response was not valid.").put(0x19, "Duplicated label.")
				.put(0x22, "Remote rack fault.").put(0x23, "Timeout.").put(0x1A, "File is open; another node owns it.")
				.put(0x1B, "Another node is the program owner.").put(0x1E, "Data table element protection violation.")
				.put(0x1F, "Temporary internal problem.").build();
	}

	public static String getName(int statusCode) {
		return STATUS_CODES.getOrDefault(statusCode, "Unknown error response.");
	}
}