package com.digitalpetri.enip.commands;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class RegisterSessionTest extends SerializationTest<RegisterSession> {

    @Test
    public void testDefaultParameters() {
        RegisterSession command = new RegisterSession();

        assertEquals(command.getProtocolVersion(), RegisterSession.DEFAULT_PROTOCOL_VERSION);
        assertEquals(command.getOptionFlags(), RegisterSession.DEFAULT_OPTION_FLAGS);
    }

    @Test(dataProvider = "getParameters")
    public void testSerialization(int protocolVersion, int optionFlags) {
        RegisterSession command = new RegisterSession(protocolVersion, optionFlags);

        RegisterSession decoded = encodeDecode(command, RegisterSession::encode, RegisterSession::decode);

        assertEquals(command.getProtocolVersion(), decoded.getProtocolVersion());
        assertEquals(command.getOptionFlags(), decoded.getOptionFlags());
    }

    @DataProvider
    private static Object[][] getParameters() {
        return new Object[][]{
            {RegisterSession.DEFAULT_PROTOCOL_VERSION, RegisterSession.DEFAULT_OPTION_FLAGS},
            {RegisterSession.DEFAULT_PROTOCOL_VERSION + 1, RegisterSession.DEFAULT_OPTION_FLAGS + 1}
        };
    }

}
