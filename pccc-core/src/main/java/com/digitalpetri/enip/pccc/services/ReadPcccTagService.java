package com.digitalpetri.enip.pccc.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.digitalpetri.enip.pccc.PcccResponseException;

import io.netty.buffer.ByteBuf;

//refer to http://literature.rockwellautomation.com/idc/groups/literature/documents/rm/1770-rm516_-en-p.pdf
//refer section typed read (read block)
public class ReadPcccTagService implements PcccService<ByteBuf> {
	private static final String TAG_PATTERN = "(\\D*?)(\\d+):(\\d+)[/.]?(\\D*)";
	private static final Pattern REGEX_PATTERN = Pattern.compile(TAG_PATTERN);

	public static final int FUNCTION_CODE = 0x68;
	public static final int PCCC_OFFSET = 0x0;

	private final int tagFileNumber;
	private final int tagElementNumber;
	private final String tagSubElementStr;
	private final int elementCount;

	/**
	 * Create a ReadPcccTagService requesting 1 element at {@code requestPath}.
	 *
	 * @param requestPath
	 *            the path to the tag to read.
	 */
	public ReadPcccTagService(String tagName) {
		this(tagName, 1);
	}

	/**
	 * Create a ReadPcccTagService requesting {@code elementCount} elements at
	 * {@code requestPath}.
	 *
	 * @param requestPath
	 *            the path to the tag to read.
	 * @param elementCount
	 *            the number of elements to request.
	 */
	public ReadPcccTagService(String tagName, int elementCount) {
		String trimmedTagName = tagName.replaceAll("\\s", "");

		Matcher matcher = REGEX_PATTERN.matcher(trimmedTagName);
		if (!matcher.matches())
			throw new IllegalArgumentException("Invalid PCCC tag name: " + tagName);

		this.tagFileNumber = Integer.parseInt(matcher.group(2));
		this.tagElementNumber = Integer.parseInt(matcher.group(3));
		this.tagSubElementStr = matcher.group(4).toLowerCase();
		this.elementCount = elementCount;
	}

	@Override
	public void encodeRequest(ByteBuf buffer) {
		buffer.writeByte(FUNCTION_CODE);
		buffer.writeShort(PCCC_OFFSET);
		buffer.writeShort(elementCount);

		// write tag name and length
		encodeTag(buffer);
		buffer.writeShort(elementCount);
	}

	@Override
	public ByteBuf decodeResponse(ByteBuf buffer) throws PartialResponseException, PcccResponseException {
		return buffer;
	}

	private void encodeTag(ByteBuf buffer) {
		int subElementNumber = -1;

		if (!tagSubElementStr.isEmpty()) {
			switch (tagSubElementStr) {
			case "acc":
			case "pos":
			case "sp":
				subElementNumber = 2;
				break;

			case "len":
			case "pre":
				subElementNumber = 1;
				break;
			}
		}

		int levelByte = 0x7;
		if (subElementNumber > -1)
			levelByte = 0xF;

		buffer.writeByte(levelByte);

		// level 1
		buffer.writeByte(0x0);

		// level 2
		if (tagFileNumber < 255)
			buffer.writeByte(tagFileNumber);
		else {
			buffer.writeByte(0xFF);
			buffer.writeShort(tagFileNumber);
		}

		// level 3
		if (tagElementNumber < 255)
			buffer.writeByte(tagElementNumber);
		else {
			buffer.writeByte(0xFF);
			buffer.writeShort(tagElementNumber);
		}

		// level 4
		if (subElementNumber > -1)
			buffer.writeByte(subElementNumber);
	}
}