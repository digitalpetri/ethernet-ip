package com.digitalpetri.enip.commands;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class NopTest extends SerializationTest<Nop> {

    @Test(dataProvider = "getData")
    public void testSerialization(byte[] data) {
        Nop command = new Nop(data);
        Nop decoded = encodeDecode(command, Nop::encode, Nop::decode);

        assertEquals(command, decoded);
    }

    @DataProvider
    private static Object[][] getData() {
        return new Object[][]{
            {new byte[0]},
            {new byte[]{1, 2, 3, 4}}
        };
    }

}
