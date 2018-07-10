# Logix Services

These services are described in detail in 1756-PM020 "Logix5000 Data Access"

#### Initial Setup

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

CipConnectionPool pool = new CipConnectionPool(2, client, connectionPath, 500);

// the tag we'll use as an example
PaddedEPath requestPath = new PaddedEPath(
        new AnsiDataSegment("My_DInt_Tag"));
```

#### Connected ReadTagService
```java
ReadTagService service = new ReadTagService(requestPath);

pool.acquire().whenComplete((connection, ex) -> {
    if (connection != null) {
        CompletableFuture<ByteBuf> f = client.invokeConnected(connection.getO2tConnectionId(), service);

        f.whenComplete((data, ex2) -> {
            if (data != null) {
                System.out.println("Tag data: " + ByteBufUtil.hexDump(data));
            } else {
                ex2.printStackTrace();
            }
            pool.release(connection);
        });
    } else {
        ex.printStackTrace();
    }
});
```

#### Connected WriteTagService
```java
ByteBuf buffer = Unpooled.buffer().order(ByteOrder.LITTLE_ENDIAN);
buffer.writeInt(42);

WriteTagService service = new WriteTagService(
        requestPath,
        false,
        CipDataType.DINT.getCode(),
        buffer
);

pool.acquire().thenAccept(connection -> {
    CompletableFuture<Void> f = client.invokeConnected(
            connection.getO2tConnectionId(), service);

    f.whenComplete((v, ex) -> {
        if (ex != null) {
            ex.printStackTrace();
        } else {
            System.out.println("WriteTagService completed.");
        }

        pool.release(connection);
    });
});

```
