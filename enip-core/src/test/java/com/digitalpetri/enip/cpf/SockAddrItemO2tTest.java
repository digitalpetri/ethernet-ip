package com.digitalpetri.enip.cpf;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SockAddrItemO2tTest extends SerializationTest<SockAddrItemO2t> {

    @Test
    public void testSerialization() {
        SockAddrItemO2t item = new SockAddrItemO2t(new SockAddr(1, 2, new byte[]{1, 2, 3, 4}, 0L));
        SockAddrItemO2t decoded = encodeDecode(item, SockAddrItemO2t::encode, SockAddrItemO2t::decode);

        assertEquals(item, decoded);
    }

}
