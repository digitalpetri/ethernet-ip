package com.digitalpetri.enip.cpf;

import com.digitalpetri.enip.SerializationTest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class UnconnectedDataItemResponseTest extends SerializationTest<UnconnectedDataItemResponse> {

    @Test(dataProvider = "getData")
    public void testSerialization(ByteBuf data) {
        data.retain();
        data.markReaderIndex();

        UnconnectedDataItemResponse item = new UnconnectedDataItemResponse(data);
        UnconnectedDataItemResponse decoded = encodeDecode(item, UnconnectedDataItemResponse::encode, UnconnectedDataItemResponse::decode);

        data.resetReaderIndex();
        assertEquals(item, decoded);

        data.release();
        decoded.getData().release();
    }

    @DataProvider
    private static Object[][] getData() {
        return new Object[][]{
            {Unpooled.EMPTY_BUFFER},
            {Unpooled.buffer().writeByte(1).writeByte(2)}
        };
    }

}
