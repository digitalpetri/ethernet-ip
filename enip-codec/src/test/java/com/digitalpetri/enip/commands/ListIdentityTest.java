package com.digitalpetri.enip.commands;

import com.digitalpetri.enip.SerializationTest;
import com.digitalpetri.enip.cpf.CipIdentityItem;
import com.digitalpetri.enip.cpf.SockAddr;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ListIdentityTest extends SerializationTest<ListIdentity> {

    @Test(dataProvider = "getIdentityItem")
    public void testSerialization(CipIdentityItem identityItem) {
        ListIdentity identity = new ListIdentity(identityItem);
        ListIdentity decoded = encodeDecode(identity, ListIdentity::encode, ListIdentity::decode);

        assertEquals(identity, decoded);
    }

    @DataProvider
    private static Object[][] getIdentityItem() {
        CipIdentityItem identityItem = new CipIdentityItem(
                0, new SockAddr(1, 2, new byte[4], 0), 1, 2, 3,
                (short) 4, (short) 5, (short) 6, 1234L, "test", (short) 0);

        return new Object[][]{
                {null},
                {identityItem}
        };
    }

}
