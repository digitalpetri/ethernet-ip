package com.digitalpetri.enip.cpf;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CipIdentityItemTest extends SerializationTest<CipIdentityItem> {

    @Test
    public void testSerialization() {
        CipIdentityItem item = new CipIdentityItem(
            0, new SockAddr(1, 2, new byte[4], 0), 1, 2, 3,
            (short) 4, (short) 5, (short) 6, 1234L, "test", (short) 0);

        CipIdentityItem decoded = encodeDecode(item, CipIdentityItem::encode, CipIdentityItem::decode);

        assertEquals(item, decoded);
    }

}
