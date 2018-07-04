package com.digitalpetri.enip;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class EnipStatusTest extends SerializationTest<EnipStatus> {

    @Test
    public void testSerialization() {
        for (EnipStatus status : EnipStatus.values()) {
            EnipStatus decoded = encodeDecode(status, EnipStatus::encode, EnipStatus::decode);

            assertEquals(status, decoded);
        }
    }

}
