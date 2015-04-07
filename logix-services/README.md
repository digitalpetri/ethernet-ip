# Logix Services
#### Example

```java
PaddedEPath requestPath = new PaddedEPath(
        new AnsiDataSegment("My_Int_Tag"));

ReadTagService service = new ReadTagService(requestPath);

client.invokeUnconnected(service).whenComplete((data, ex) -> {
    if (data != null) {
        System.out.println("Tag data: " + ByteBufUtil.hexDump(data));
    } else {
        ex.printStackTrace();
    }
});
```
