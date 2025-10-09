package com.digitalpetri.enip.commands;

import static org.testng.Assert.assertEquals;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.Test;

public class CommandCodeTest extends SerializationTest<CommandCode> {

  @Test
  public void testSerialization() {
    for (CommandCode commandCode : CommandCode.values()) {
      CommandCode decoded = encodeDecode(commandCode, CommandCode::encode, CommandCode::decode);

      assertEquals(commandCode, decoded);
    }
  }
}
