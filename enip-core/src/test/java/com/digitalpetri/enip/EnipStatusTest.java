package com.digitalpetri.enip;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class EnipStatusTest extends SerializationTest<EnipStatus> {

  @Test
  public void testSerialization() {
    for (EnipStatus status : EnipStatus.values()) {
      EnipStatus decoded = encodeDecode(status, EnipStatus::encode, EnipStatus::decode);

      assertEquals(status, decoded);
    }
  }
}
