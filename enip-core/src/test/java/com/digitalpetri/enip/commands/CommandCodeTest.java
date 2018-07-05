package com.digitalpetri.enip.commands;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CommandCodeTest extends SerializationTest<CommandCode> {

    @Test
    public void testSerialization() {
        for (CommandCode commandCode : CommandCode.values()) {
            CommandCode decoded = encodeDecode(commandCode, CommandCode::encode, CommandCode::decode);

            assertEquals(commandCode, decoded);
        }
    }

}
