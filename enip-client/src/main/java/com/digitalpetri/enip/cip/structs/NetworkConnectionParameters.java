package com.digitalpetri.enip.cip.structs;

public class NetworkConnectionParameters {

    private final int connectionSize;
    private final SizeType sizeType;
    private final Priority priority;
    private final ConnectionType connectionType;
    private final boolean redundantOwner;

    public NetworkConnectionParameters(int connectionSize,
                                       SizeType sizeType,
                                       Priority priority,
                                       ConnectionType connectionType,
                                       boolean redundantOwner) {

        this.connectionSize = connectionSize;
        this.sizeType = sizeType;
        this.priority = priority;
        this.connectionType = connectionType;
        this.redundantOwner = redundantOwner;
    }

    public int getConnectionSize() {
        return connectionSize;
    }

    public SizeType getSizeType() {
        return sizeType;
    }

    public Priority getPriority() {
        return priority;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public boolean isRedundantOwner() {
        return redundantOwner;
    }

    public static enum SizeType {
        Fixed,
        Variable
    }

    public static enum Priority {
        Low,
        High,
        Scheduled,
        Urgent
    }

    public static enum ConnectionType {
        Null,
        Multicast,
        PointToPoint,
        Reserved
    }

}
