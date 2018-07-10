package com.digitalpetri.enip.fsm;

import java.time.Duration;

import com.digitalpetri.enip.EtherNetIpClient;
import com.digitalpetri.enip.EtherNetIpClientConfig;
import org.testng.annotations.Test;

public class ChannelFsmTest {

    private final EtherNetIpClient client = new EtherNetIpClient(
        EtherNetIpClientConfig.builder("")
            .setSerialNumber(0x00)
            .setVendorId(0x00)
            .setTimeout(Duration.ofSeconds(2))
            .build()
    );

    @Test
    public void testLazyBehavior() {
        // a Lazy ChannelFsm goes from Reconnecting to Idle on a ConnectFailure event
    }

    @Test
    public void testPersistentBehavior() {
        // a Persistent ChannelFsm keeps goes from Connecting to Reconnecting on a ConnectFailure event.
    }

    @Test
    public void testLazyPersistentBehavior() {
        // a Lazy+Persistent goes from (Connecting|Reconnecting) to Idle on a ConnectFailure event.
    }

}
