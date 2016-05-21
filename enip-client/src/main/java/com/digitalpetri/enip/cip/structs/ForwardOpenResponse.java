package com.digitalpetri.enip.cip.structs;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public final class ForwardOpenResponse {

    private final int o2tConnectionId;
    private final int t2oConnectionId;
    private final int connectionSerialNumber;
    private final int originatorVendorId;
    private final long originatorSerialNumber;
    private final Duration o2tApi;
    private final Duration t2oApi;
    private final int applicationReplySize;
    private final ByteBuf applicationReply;

    public ForwardOpenResponse(int o2tConnectionId,
                               int t2oConnectionId,
                               int connectionSerialNumber,
                               int originatorVendorId,
                               long originatorSerialNumber,
                               Duration o2tApi,
                               Duration t2oApi,
                               int applicationReplySize,
                               ByteBuf applicationReply) {

        this.o2tConnectionId = o2tConnectionId;
        this.t2oConnectionId = t2oConnectionId;
        this.connectionSerialNumber = connectionSerialNumber;
        this.originatorVendorId = originatorVendorId;
        this.originatorSerialNumber = originatorSerialNumber;
        this.o2tApi = o2tApi;
        this.t2oApi = t2oApi;
        this.applicationReplySize = applicationReplySize;
        this.applicationReply = applicationReply;
    }

    public int getO2tConnectionId() {
        return o2tConnectionId;
    }

    public int getT2oConnectionId() {
        return t2oConnectionId;
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

    public Duration getO2tApi() {
        return o2tApi;
    }

    public Duration getT2oApi() {
        return t2oApi;
    }

    public int getApplicationReplySize() {
        return applicationReplySize;
    }

    public ByteBuf getApplicationReply() {
        return applicationReply;
    }

    public static ForwardOpenResponse decode(ByteBuf buffer) {
        int o2tConnectionId = buffer.readInt();
        int t2oConnectionId = buffer.readInt();
        int connectionSerialNumber = buffer.readUnsignedShort();
        int originatorVendorId = buffer.readUnsignedShort();
        long originatorSerialNumber = buffer.readUnsignedInt();
        long o2tActualPacketInterval = TimeUnit.MICROSECONDS
            .convert(buffer.readUnsignedInt(), TimeUnit.MILLISECONDS);
        long t2oActualPacketInterval = TimeUnit.MICROSECONDS
            .convert(buffer.readUnsignedInt(), TimeUnit.MILLISECONDS);
        int applicationReplySize = buffer.readUnsignedByte();
        buffer.skipBytes(1); // reserved

        ByteBuf applicationReply = applicationReplySize > 0 ?
            buffer.readSlice(applicationReplySize).copy() :
            Unpooled.EMPTY_BUFFER;

        return new ForwardOpenResponse(
            o2tConnectionId,
            t2oConnectionId,
            connectionSerialNumber,
            originatorVendorId,
            originatorSerialNumber,
            Duration.ofMillis(o2tActualPacketInterval),
            Duration.ofMillis(t2oActualPacketInterval),
            applicationReplySize,
            applicationReply
        );
    }

}
