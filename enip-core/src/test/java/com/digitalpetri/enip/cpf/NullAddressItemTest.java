package com.digitalpetri.enip.cpf;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class NullAddressItemTest extends SerializationTest<NullAddressItem> {

    @Test
    public void testSerialization() {
        NullAddressItem item = new NullAddressItem();
        NullAddressItem decoded = encodeDecode(item, NullAddressItem::encode, NullAddressItem::decode);

        assertEquals(item, decoded);
    }

}
