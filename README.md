# EtherNet/IP Client
[![Build status](https://travis-ci.org/digitalpetri/ethernet-ip.svg?branch=master)](https://travis-ci.org/digitalpetri/ethernet-ip)
[![Maven Central](https://img.shields.io/maven-central/v/com.digitalpetri.enip/enip.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.digitalpetri.enip%22%20AND%20a%3A%22enip%22)

Asynchronous, non-blocking, EtherNet/IP client implementation for Java

# Maven

## EtherNet/IP Client
```xml
<dependency>
    <groupId>com.digitalpetri.enip</groupId>
    <artifactId>enip-client</artifactId>
    <version>1.3.4</version>
</dependency>
```

## CIP Client
```xml
<dependency>
    <groupId>com.digitalpetri.enip</groupId>
    <artifactId>cip-client</artifactId>
    <version>1.3.4</version>
</dependency>
```

# Quick Start
#### EtherNet/IP Example
```java
EtherNetIpClientConfig config = EtherNetIpClientConfig.builder("10.20.4.57")
        .setSerialNumber(0x00)
        .setVendorId(0x00)
        .setTimeout(Duration.ofSeconds(2))
        .build();

EtherNetIpClient client = new EtherNetIpClient(config);

client.connect().get();

client.listIdentity().whenComplete((li, ex) -> {
    if (li != null) {
        li.getIdentity().ifPresent(id -> {
            System.out.println("productName=" + id.getProductName());
            System.out.println("revisionMajor=" + id.getRevisionMajor());
            System.out.println("revisionMinor=" + id.getRevisionMinor());
        });
    } else {
        ex.printStackTrace();
    }
});

client.disconnect().get();

// Call this before application / JVM shutdown
EtherNetIpShared.releaseSharedResources();
```
#### CIP Service Example
```java
EtherNetIpClientConfig config = EtherNetIpClientConfig.builder("10.20.4.57")
        .setSerialNumber(0x00)
        .setVendorId(0x00)
        .setTimeout(Duration.ofSeconds(2))
        .build();

// backplane, slot 0
PaddedEPath connectionPath = new PaddedEPath(
        new PortSegment(1, new byte[]{(byte) 0}));

CipClient client = new CipClient(config, connectionPath);

client.connect().get();

GetAttributeListService service = new GetAttributeListService(
        new PaddedEPath(new ClassId(0x01), new InstanceId(0x01)),
        new int[]{4},
        new int[]{2}
);

client.invokeUnconnected(service).whenComplete((as, ex) -> {
    if (as != null) {
        try {
            ByteBuf data = as[0].getData();
            int major = data.readUnsignedByte();
            int minor = data.readUnsignedByte();

            System.out.println(String.format("firmware v%s.%s", major, minor));
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            Arrays.stream(as).forEach(a -> ReferenceCountUtil.release(a.getData()));
        }
    } else {
        ex.printStackTrace();
    }
});

client.disconnect().get();

// Call this before application / JVM shutdown
EtherNetIpShared.releaseSharedResources();
```

#### Logix Example

[See the logix-services README!](logix-services/README.md)


License
--------

Apache License, Version 2.0
