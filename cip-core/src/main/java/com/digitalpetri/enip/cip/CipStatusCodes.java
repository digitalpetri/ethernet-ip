package com.digitalpetri.enip.cip;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class CipStatusCodes {


    private CipStatusCodes() {
    }

    private static final Map<Integer, NameAndDescription> STATUS_CODES;

    static {
        STATUS_CODES = new HashMap<>();
        STATUS_CODES.put(0x00, v("success", "Service was successfully performed by the object specified."));
        STATUS_CODES.put(0x01, v("connection failure", "A connection related service failed along the connection path."));
        STATUS_CODES.put(0x02, v("resource unavailable", "Resources needed for the object to perform the requested service were unavailable."));
        STATUS_CODES.put(0x03, v("invalid parameter value", "A parameter associated with the request was invalid. This code is used when a parameter does not meet the requirements of this specification and/or the requirements defined in an Application Object Specification."));
        STATUS_CODES.put(0x04, v("path segment error", "The path segment identifier or the segment syntax was not understood by the processing node. Path processing shall stop when a path segment error is encountered."));
        STATUS_CODES.put(0x05, v("path destination unknown", "The path is referencing an object class, instance or structure element that is not known or is not contained in the processing node. Path processing shall stop when a path destination unknown error is encountered."));
        STATUS_CODES.put(0x06, v("partial transfer", "Only part of the expected data was transferred."));
        STATUS_CODES.put(0x07, v("connection lost", "The messaging connection was lost."));
        STATUS_CODES.put(0x08, v("service not supported", "The requested service was not implemented or was not defined for this Object Class/Instance."));
        STATUS_CODES.put(0x09, v("invalid attribute value", "Invalid attribute data detected."));
        STATUS_CODES.put(0x0A, v("attribute list error", "An attribute in the Get_Attribute_List or Set_Attribute_List response has a non-zero status."));
        STATUS_CODES.put(0x0B, v("already in requested mode/state", "The object is already in the mode/state being requested by the service."));
        STATUS_CODES.put(0x0C, v("object state conflict", "The object cannot perform the requested service in its current mode/state."));
        STATUS_CODES.put(0x0D, v("object already exists", "The requested instance of object to be created already exists."));
        STATUS_CODES.put(0x0E, v("attribute not settable", "A request to modify a non-modifiable attribute was received."));
        STATUS_CODES.put(0x0F, v("privilege violation", "A permission/privilege check failed."));
        STATUS_CODES.put(0x10, v("device state conflict", "The device’s current mode/state prohibits the execution of the requested service."));
        STATUS_CODES.put(0x11, v("reply data too large", "The data to be transmitted in the response buffer is larger than the allocated response buffer."));
        STATUS_CODES.put(0x12, v("fragmentation of a primitive value", "The service specified an operation that is going to fragment a primitive data value, i.e. half a REAL data type."));
        STATUS_CODES.put(0x13, v("not enough data", "The service did not supply enough data to perform the specified operation."));
        STATUS_CODES.put(0x14, v("attribute not supported", "The attribute specified in the request is not supported."));
        STATUS_CODES.put(0x15, v("too much data", "The service supplied more data than was expected."));
        STATUS_CODES.put(0x16, v("object does not exist", "The object specified does not exist in the device."));
        STATUS_CODES.put(0x17, v("service fragmentation sequence not in progress", "The fragmentation sequence for this service is not currently active for this data."));
        STATUS_CODES.put(0x18, v("no stored attribute data", "The attribute data of this object was not saved prior to the requested service."));
        STATUS_CODES.put(0x19, v("store operation failure", "The attribute data of this object was not saved due to a failure during the attempt."));
        STATUS_CODES.put(0x1A, v("routing failure, request packet too large", "The service request packet was too large for transmission on a network in the path to the destination. The routing device was forced to abort the service."));
        STATUS_CODES.put(0x1B, v("routing failure, response packet too large", "The service response packet was too large for transmission on a network in the path from the destination. The routing device was forced to abort the service."));
        STATUS_CODES.put(0x1C, v("missing attribute list entry data", "The service did not supply an attribute in a list of attributes that was needed by the service to perform the requested behavior."));
        STATUS_CODES.put(0x1D, v("invalid attribute value list", "The service is returning the list of attributes supplied with status information for those attributes that were invalid."));
        STATUS_CODES.put(0x1E, v("embedded service error", "An embedded service resulted in an error."));
        STATUS_CODES.put(0x1F, v("vendor specific error", "A vendor specific error has been encountered."));
        STATUS_CODES.put(0x20, v("invalid parameter", "A parameter associated with the request was invalid. This code is used when a parameter does not meet the requirements of this specification and/or the requirements defined in an Application Object Specification."));
        STATUS_CODES.put(0x21, v("write-once value or medium already written", "An attempt was made to write to a write-once medium (e.g. WORM drive, PROM) that has already been written, or to modify a value that cannot be changed once established."));
        STATUS_CODES.put(0x22, v("invalid reply received", "An invalid reply is received (e.g. reply service code does not match the request service code, or reply message is shorter than the minimum expected reply size). This status code can serve for other causes of invalid replies."));
        STATUS_CODES.put(0x23, v("buffer overflow", "The message received is larger than the receiving buffer can handle. The entire message was discarded."));
        STATUS_CODES.put(0x24, v("message format error", "The format of the received message is not supported by the server."));
        STATUS_CODES.put(0x25, v("key failure in path", "The Key Segment that was included as the first segment in the path does not match the destination module. The object specific status shall indicate which part of the key check failed."));
        STATUS_CODES.put(0x26, v("path size invalid", "The size of the path which was sent with the Service Request is either not large enough to allow the Request to be routed to an object or too much routing data was included."));
        STATUS_CODES.put(0x27, v("unexpected attribute in list", "An attempt was made to set an attribute that is not able to be set at this time."));
        STATUS_CODES.put(0x28, v("invalid member id", "The Member ID specified in the request does not exist in the specified Class/Instance/Attribute."));
        STATUS_CODES.put(0x29, v("member not settable", "A request to modify a non-modifiable member was received."));
        STATUS_CODES.put(0x2A, v("group 2 only server general failure", "This error code may only be reported by DeviceNet Group 2 Only servers with 4K or less code space and only in place of Service not supported, Attribute not supported and Attribute not settable."));
        STATUS_CODES.put(0x2B, v("unknown modbus error", "A CIP to Modbus translator received an unknown Modbus Exception Code."));
        STATUS_CODES.put(0x2C, v("attribute not gettable", "A request to read a non-readable attribute was received."));
        STATUS_CODES.put(0x2D, v("instance not deletable", "The requested object instance cannot be deleted."));
    }

    public static Optional<String> getName(int statusCode) {
        NameAndDescription nameAndDescription =
            STATUS_CODES.getOrDefault(statusCode, NameAndDescription.NULL);

        return Optional.ofNullable(nameAndDescription.name);
    }

    public static Optional<String> getDescription(int statusCode) {
        NameAndDescription nameAndDescription =
            STATUS_CODES.getOrDefault(statusCode, NameAndDescription.NULL);

        return Optional.ofNullable(nameAndDescription.description);
    }

    private static NameAndDescription v(String name, String description) {
        return new NameAndDescription(name, description);
    }

    private static class NameAndDescription {
        static final NameAndDescription NULL = new NameAndDescription(null, null);

        final String name;
        final String description;

        public NameAndDescription(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

}
