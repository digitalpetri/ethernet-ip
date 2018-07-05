package com.digitalpetri.enip.commands;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ListInterfacesTest extends SerializationTest<ListInterfaces> {

    @Test(dataProvider = "getInterfaces")
    public void testSerialization(ListInterfaces.InterfaceInformation[] interfaces) {
        ListInterfaces command = new ListInterfaces(interfaces);
        ListInterfaces decoded = encodeDecode(command, ListInterfaces::encode, ListInterfaces::decode);

        assertEquals(command, decoded);
    }

    @DataProvider
    private static Object[][] getInterfaces() {
        ListInterfaces.InterfaceInformation ii1 = new ListInterfaces.InterfaceInformation(1, new byte[]{1, 2, 3, 4});
        ListInterfaces.InterfaceInformation ii2 = new ListInterfaces.InterfaceInformation(2, new byte[]{4, 3, 2, 1});

        return new Object[][]{
            {new ListInterfaces.InterfaceInformation[0]},
            {new ListInterfaces.InterfaceInformation[]{ii1}},
            {new ListInterfaces.InterfaceInformation[]{ii1, ii2}}
        };
    }
}
