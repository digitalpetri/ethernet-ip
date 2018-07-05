package com.digitalpetri.enip.cpf;

import com.digitalpetri.enip.SerializationTest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ConnectedDataItemResponseTest extends SerializationTest<ConnectedDataItemResponse> {

    @Test(dataProvider = "getData")
    public void testSerialization(ByteBuf data) {
        data.retain();
        data.markReaderIndex();

        ConnectedDataItemResponse item = new ConnectedDataItemResponse(data);
        ConnectedDataItemResponse decoded = encodeDecode(item, ConnectedDataItemResponse::encode, ConnectedDataItemResponse::decode);

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
