package com.digitalpetri.enip.cip.structs;

import java.time.Duration;

import com.digitalpetri.enip.cip.epath.DataSegment;
import com.digitalpetri.enip.cip.epath.EPath;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.epath.EPathSegment;
import com.digitalpetri.enip.cip.epath.LogicalSegment;
import com.digitalpetri.enip.cip.epath.PortSegment;
import com.digitalpetri.enip.util.TimeoutCalculator;
import io.netty.buffer.ByteBuf;

public class ForwardCloseRequest {

    private final Duration connectionTimeout;
    private final int connectionSerialNumber;
    private final int originatorVendorId;
    private final long originatorSerialNumber;
    private final PaddedEPath connectionPath;

    public ForwardCloseRequest(Duration connectionTimeout,
                               int connectionSerialNumber,
                               int originatorVendorId,
                               long originatorSerialNumber,
                               PaddedEPath connectionPath) {

        this.connectionTimeout = connectionTimeout;
        this.connectionSerialNumber = connectionSerialNumber;
        this.originatorVendorId = originatorVendorId;
        this.originatorSerialNumber = originatorSerialNumber;
        this.connectionPath = connectionPath;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getConnectionSerialNumber() {
        return connectionSerialNumber;
    }

    public int getOriginatorVendorId() {
        return originatorVendorId;
    }

    public long getOriginatorSerialNumber() {
        return originatorSerialNumber;
    }

    public PaddedEPath getConnectionPath() {
        return connectionPath;
    }

    public static ByteBuf encode(ForwardCloseRequest request, ByteBuf buffer) {
        int priorityAndTimeoutBytes = TimeoutCalculator.calculateTimeoutBytes(request.getConnectionTimeout());
        buffer.writeByte(priorityAndTimeoutBytes >> 8 & 0xFF);
        buffer.writeByte(priorityAndTimeoutBytes & 0xFF);

        buffer.writeShort(request.getConnectionSerialNumber());

        buffer.writeShort(request.getOriginatorVendorId());
        buffer.writeInt((int) request.getOriginatorSerialNumber());

        encodeConnectionPath(request.getConnectionPath(), buffer);

        return buffer;
    }

    /**
     * Encode the connection path.
     * <p>
     * {@link PaddedEPath#encode(EPath, ByteBuf)} can't be used here because the {@link ForwardCloseRequest} has an
     * extra reserved byte after the connection path size for some reason.
     *
     * @param path   the {@link PaddedEPath} to encode.
     * @param buffer the {@link ByteBuf} to encode into.
     */
    private static void encodeConnectionPath(PaddedEPath path, ByteBuf buffer) {
        // length placeholder...
        int lengthStartIndex = buffer.writerIndex();
        buffer.writeByte(0);

        // reserved
        buffer.writeZero(1);

        // encode the path segments...
        int dataStartIndex = buffer.writerIndex();

        for (EPathSegment segment : path.getSegments()) {
            if (segment instanceof LogicalSegment) {
                LogicalSegment.encode((LogicalSegment) segment, path.isPadded(), buffer);
            } else if (segment instanceof PortSegment) {
                PortSegment.encode((PortSegment) segment, path.isPadded(), buffer);
            } else if (segment instanceof DataSegment) {
                DataSegment.encode((DataSegment) segment, path.isPadded(), buffer);
            } else {
                throw new RuntimeException("no encoder for " + segment.getClass().getSimpleName());
            }
        }

        // go back and update the length
        int bytesWritten = buffer.writerIndex() - dataStartIndex;
        int wordsWritten = bytesWritten / 2;
        buffer.markWriterIndex();
        buffer.writerIndex(lengthStartIndex);
        buffer.writeByte(wordsWritten);
        buffer.resetWriterIndex();
    }

}
