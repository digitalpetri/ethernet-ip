package com.digitalpetri.enip.cip.epath;

import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class DataSegment<T> extends EPathSegment {

  public static final int SEGMENT_TYPE = 0x80;

  protected abstract ByteBuf encode(ByteBuf buffer);

  public static ByteBuf encode(DataSegment<?> segment, boolean padded, ByteBuf buffer) {
    return segment.encode(buffer);
  }

  public static final class AnsiDataSegment extends DataSegment<String> {

    public static final int SUBTYPE = 0x11;

    private final String data;
    private final Charset charset;

    public AnsiDataSegment(String data) {
      this(data, StandardCharsets.US_ASCII);
    }

    public AnsiDataSegment(String data, Charset charset) {
      this.data = data;
      this.charset = charset;
    }

    /**
     * Get the data string.
     *
     * @return the data string.
     */
    public String getData() {
      return data;
    }

    /**
     * Get the charset used to encode the data.
     *
     * @return the charset.
     */
    public Charset getCharset() {
      return charset;
    }

    @Override
    protected ByteBuf encode(ByteBuf buffer) {
      byte[] dataBytes = data.getBytes(charset);
      int dataLength = Math.min(dataBytes.length, 255);

      buffer.writeByte(SEGMENT_TYPE | SUBTYPE);
      buffer.writeByte(dataLength);
      buffer.writeBytes(dataBytes, 0, dataLength);
      if (dataLength % 2 != 0) buffer.writeByte(0);

      return buffer;
    }
  }

  public static final class SimpleDataSegment extends DataSegment<short[]> {

    private final short[] data;

    public SimpleDataSegment(short[] data) {
      this.data = data;
    }

    /**
     * Get the data array.
     *
     * @return the data array.
     */
    public short[] getData() {
      return data;
    }

    @Override
    protected ByteBuf encode(ByteBuf buffer) {
      buffer.writeByte(SEGMENT_TYPE);
      buffer.writeByte(data.length);

      for (short d : data) {
        buffer.writeShort(d);
      }

      return buffer;
    }
  }
}
