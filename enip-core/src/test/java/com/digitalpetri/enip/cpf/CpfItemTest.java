package com.digitalpetri.enip.cpf;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CpfItemTest extends SerializationTest<CpfItem> {

    @Test(dataProvider = "getItems")
    public void testSerialization(CpfItem item) {
        CpfItem decoded = encodeDecode(item, CpfItem::encode, CpfItem::decode);

        assertEquals(item, decoded);
    }

    @DataProvider
    private static Object[][] getItems() {
        return new Object[][]{
            {new CipIdentityItem(
                0, new SockAddr(1, 2, new byte[4], 0), 1, 2, 3,
                (short) 4, (short) 5, (short) 6, 1234L, "test", (short) 0)},
            {new ConnectedAddressItem(1)},
            {new NullAddressItem()},
            {new SequencedAddressItem(1L, 2L)},
            {new SockAddrItemO2t(new SockAddr(1, 2, new byte[]{1, 2, 3, 4}, 0L))},
            {new SockAddrItemT2o(new SockAddr(1, 2, new byte[]{1, 2, 3, 4}, 0L))},
            {new CipSecurityItem(1, 2, 3, 4)}
        };
    }

}
