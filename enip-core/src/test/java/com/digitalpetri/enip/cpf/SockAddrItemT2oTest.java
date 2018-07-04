package com.digitalpetri.enip.cpf;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SockAddrItemT2oTest extends SerializationTest<SockAddrItemT2o> {

    @Test
    public void testSerialization() {
        SockAddrItemT2o item = new SockAddrItemT2o(new SockAddr(1, 2, new byte[]{1, 2, 3, 4}, 0L));
        SockAddrItemT2o decoded = encodeDecode(item, SockAddrItemT2o::encode, SockAddrItemT2o::decode);

        assertEquals(item, decoded);
    }

}
