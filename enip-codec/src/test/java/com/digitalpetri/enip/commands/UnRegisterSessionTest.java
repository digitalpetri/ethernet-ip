package com.digitalpetri.enip.commands;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class UnRegisterSessionTest extends SerializationTest<UnRegisterSession> {

    @Test
    public void testSerialization() {
        UnRegisterSession command = new UnRegisterSession();
        UnRegisterSession decoded = encodeDecode(command, UnRegisterSession::encode, UnRegisterSession::decode);

        assertEquals(command, decoded);
    }
}
