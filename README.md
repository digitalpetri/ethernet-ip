# EtherNet/IP Client
Asynchronous, non-blocking, EtherNet/IP client implementation for Java

# Maven

```xml
<dependency>
    <groupId>com.digitalpetri.enip</groupId>
    <artifactId>enip-client</artifactId>
    <version>1.0.0</version>
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
```
