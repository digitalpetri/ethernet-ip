package com.digitalpetri.enip.commands;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ListServicesTest extends SerializationTest<ListServices> {

    @Test(dataProvider = "getServices")
    public void testSerialization(ListServices.ServiceInformation[] services) {
        ListServices command = new ListServices(services);
        ListServices decoded = encodeDecode(command, ListServices::encode, ListServices::decode);

        assertEquals(command, decoded);
    }

    @DataProvider
    private static Object[][] getServices() {
        ListServices.ServiceInformation ii1 = new ListServices.ServiceInformation(1, 1, 2, "SomeService");
        ListServices.ServiceInformation ii2 = new ListServices.ServiceInformation(2, 1, 3, "OtherServicxe");

        return new Object[][]{
            {new ListServices.ServiceInformation[0]},
            {new ListServices.ServiceInformation[]{ii1}},
            {new ListServices.ServiceInformation[]{ii1, ii2}}
        };
    }
}
