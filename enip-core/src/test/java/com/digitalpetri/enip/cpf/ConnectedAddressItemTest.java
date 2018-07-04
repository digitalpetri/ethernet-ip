package com.digitalpetri.enip.cpf;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ConnectedAddressItemTest extends SerializationTest<ConnectedAddressItem> {

    @Test(dataProvider = "getConnectionId")
    public void testSerialization(int connectionId) {
        ConnectedAddressItem item = new ConnectedAddressItem(connectionId);

        ConnectedAddressItem decoded = encodeDecode(item, ConnectedAddressItem::encode, ConnectedAddressItem::decode);

        assertEquals(item, decoded);
    }

    @DataProvider
    private static Object[][] getConnectionId() {
        return new Object[][]{
            {0},
            {1},
            {Integer.MAX_VALUE}
        };
    }

}
