package com.digitalpetri.enip.cpf;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SequencedAddressItemTest extends SerializationTest<SequencedAddressItem> {

    @Test(dataProvider = "getParameters")
    public void testSerialization(long connectionId, long sequenceNumber) {
        SequencedAddressItem item = new SequencedAddressItem(connectionId, sequenceNumber);
        SequencedAddressItem decoded = encodeDecode(item, SequencedAddressItem::encode, SequencedAddressItem::decode);

        assertEquals(item, decoded);
    }

    @DataProvider
    private static Object[][] getParameters() {
        return new Object[][]{
            {0L, 0L},
            {(long) Short.MAX_VALUE, (long) Short.MAX_VALUE},
            {(long) Integer.MAX_VALUE, (long) Integer.MAX_VALUE},
            {(long) Integer.MAX_VALUE + 1, (long) Integer.MAX_VALUE + 1}
        };
    }

}
