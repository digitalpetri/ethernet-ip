package com.digitalpetri.enip.cpf;

import static org.testng.Assert.assertEquals;

import com.digitalpetri.enip.SerializationTest;
import org.testng.annotations.Test;

public class NullAddressItemTest extends SerializationTest<NullAddressItem> {

  @Test
  public void testSerialization() {
    NullAddressItem item = new NullAddressItem();
    NullAddressItem decoded = encodeDecode(item, NullAddressItem::encode, NullAddressItem::decode);

    assertEquals(item, decoded);
  }
}
