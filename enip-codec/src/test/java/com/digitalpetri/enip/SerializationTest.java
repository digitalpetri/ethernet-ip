package com.digitalpetri.enip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class SerializationTest<T> {

    protected ByteBuf buffer;

    @BeforeMethod
    public void setUp() {
        buffer = Unpooled.buffer();
    }

    @AfterMethod
    public void tearDown() {
        ReferenceCountUtil.release(buffer);
    }

    protected T encodeDecode(T toEncode,
                             BiConsumer<T, ByteBuf> encoder,
                             Function<ByteBuf, T> decoder) {
        encoder.accept(toEncode, buffer);
        return decoder.apply(buffer);
    }

}
