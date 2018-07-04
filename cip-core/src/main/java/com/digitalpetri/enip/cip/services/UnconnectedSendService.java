package com.digitalpetri.enip.cip.services;

import java.time.Duration;

import com.digitalpetri.enip.cip.CipResponseException;
import com.digitalpetri.enip.cip.epath.DataSegment;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.epath.EPathSegment;
import com.digitalpetri.enip.cip.epath.LogicalSegment;
import com.digitalpetri.enip.cip.epath.LogicalSegment.ClassId;
import com.digitalpetri.enip.cip.epath.LogicalSegment.InstanceId;
import com.digitalpetri.enip.cip.epath.PortSegment;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.util.TimeoutCalculator;
import io.netty.buffer.ByteBuf;

public class UnconnectedSendService<T> implements CipService<T> {

    public static final int SERVICE_CODE = 0x52;

    private static final PaddedEPath CONNECTION_MANAGER_PATH = new PaddedEPath(
        new ClassId(0x06),
        new InstanceId(0x01)
    );

    private final CipService<T> service;
    private final PaddedEPath connectionPath;
    private final Duration timeout;

    public UnconnectedSendService(CipService<T> service,
                                  PaddedEPath connectionPath,
                                  Duration timeout) {

        this.service = service;
        this.connectionPath = connectionPath;
        this.timeout = timeout;
    }

    @Override
    public void encodeRequest(ByteBuf buffer) {
        MessageRouterRequest request = new MessageRouterRequest(
            SERVICE_CODE,
            CONNECTION_MANAGER_PATH,
            this::encode
        );

        MessageRouterRequest.encode(request, buffer);
    }

    private ByteBuf encode(ByteBuf buffer) {
        int priorityAndTimeoutBytes = TimeoutCalculator.calculateTimeoutBytes(timeout);

        // priority/timeTick & timeoutTicks
        buffer.writeByte(priorityAndTimeoutBytes >> 8 & 0xFF);
        buffer.writeByte(priorityAndTimeoutBytes & 0xFF);

        // message length + message
        int bytesWritten = encodeEmbeddedService(buffer);

        // pad byte if length was odd
        if (bytesWritten % 2 != 0) buffer.writeByte(0x00);

        // path length + reserved + path
        encodeConnectionPath(buffer);

        return buffer;
    }

    private int encodeEmbeddedService(ByteBuf buffer) {
        // length of embedded message
        int lengthStartIndex = buffer.writerIndex();
        buffer.writeShort(0);

        // embedded message
        int messageStartIndex = buffer.writerIndex();
        service.encodeRequest(buffer);

        // go back and update length
        int bytesWritten = buffer.writerIndex() - messageStartIndex;
        buffer.markWriterIndex();
        buffer.writerIndex(lengthStartIndex);
        buffer.writeShort(bytesWritten);
        buffer.resetWriterIndex();

        return bytesWritten;
    }

    private void encodeConnectionPath(ByteBuf buffer) {
        // connectionPath length
        int pathLengthStartIndex = buffer.writerIndex();
        buffer.writeByte(0);

        // reserved byte
        buffer.writeByte(0x00);

        // encode the path segments...
        int pathDataStartIndex = buffer.writerIndex();

        for (EPathSegment segment : connectionPath.getSegments()) {
            if (segment instanceof LogicalSegment) {
                LogicalSegment.encode((LogicalSegment) segment, connectionPath.isPadded(), buffer);
            } else if (segment instanceof PortSegment) {
                PortSegment.encode((PortSegment) segment, connectionPath.isPadded(), buffer);
            } else if (segment instanceof DataSegment) {
                DataSegment.encode((DataSegment) segment, connectionPath.isPadded(), buffer);
            } else {
                throw new RuntimeException("no encoder for " + segment.getClass().getSimpleName());
            }
        }

        // go back and update the length.
        int pathBytesWritten = buffer.writerIndex() - pathDataStartIndex;
        int wordsWritten = pathBytesWritten / 2;
        buffer.markWriterIndex();
        buffer.writerIndex(pathLengthStartIndex);
        buffer.writeByte(wordsWritten);
        buffer.resetWriterIndex();
    }

    @Override
    public T decodeResponse(ByteBuf buffer) throws CipResponseException, PartialResponseException {
        return service.decodeResponse(buffer);
    }

}
