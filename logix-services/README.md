# Logix Services
#### Example

```java
PaddedEPath requestPath = new PaddedEPath(
        new AnsiDataSegment("My_Int_Tag"));

ReadTagService service = new ReadTagService(requestPath);

// Unconnected messaging
client.invokeUnconnected(service).whenComplete((data, ex) -> {
    if (data != null) {
        System.out.println("Tag data: " + ByteBufUtil.hexDump(data));
    } else {
        ex.printStackTrace();
    }
});

// Connected messaging
CipConnectionPool pool = new CipConnectionPool(2, client, connectionPath, 500);

pool.acquire().whenComplete((connection, ex) -> {
    if (connection != null) {
        try {
            CompletableFuture<ByteBuf> f = client.invokeConnected(connection.getO2tConnectionId(), service);

            f.whenComplete((data, ex2) -> {
                if (data != null) {
                    System.out.println("Tag data: " + ByteBufUtil.hexDump(data));
                } else {
                    ex2.printStackTrace();
                }
            });
        } finally {
            pool.release(connection);
        }
    } else {
        ex.printStackTrace();
    }
});
```
