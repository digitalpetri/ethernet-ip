package com.digitalpetri.enip;

import com.digitalpetri.enip.commands.Command;
import com.digitalpetri.enip.commands.ListIdentity;
import com.digitalpetri.enip.commands.ListInterfaces;
import com.digitalpetri.enip.commands.ListServices;
import com.digitalpetri.enip.commands.Nop;
import com.digitalpetri.enip.commands.RegisterSession;
import com.digitalpetri.enip.commands.SendRRData;
import com.digitalpetri.enip.commands.SendUnitData;
import com.digitalpetri.enip.commands.UnRegisterSession;
import com.digitalpetri.enip.cpf.CpfPacket;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class EnipPacketTest extends SerializationTest<EnipPacket> {

    @Test(dataProvider = "getCommand")
    public void testSerialization(Command command) {
        EnipPacket packet = new EnipPacket(
            command.getCommandCode(),
            1L,
            EnipStatus.EIP_SUCCESS,
            2L,
            command
        );

        EnipPacket decoded = encodeDecode(packet, EnipPacket::encode, EnipPacket::decode);

        assertEquals(packet, decoded);
    }

    @DataProvider
    private static Object[][] getCommand() {
        return new Object[][]{
            {new ListIdentity()},
            {new ListInterfaces()},
            {new ListServices()},
            {new Nop()},
            {new RegisterSession()},
            {new SendRRData(new CpfPacket())},
            {new SendUnitData(new CpfPacket())},
            {new UnRegisterSession()}
        };
    }

}
