package com.digitalpetri.enip.logix.services;

import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

import com.digitalpetri.enip.logix.structs.SymbolInstance;
import io.netty.buffer.ByteBuf;

public class GetSymbolInstanceAttributeListService extends GetInstanceAttributeListService<SymbolInstance> {

    private static final int SYMBOL_CLASS_ID = 0x6B;

    private static final int[] REQUESTED_ATTRIBUTES = {
        1, // symbol name
        2, // symbol type
        8  // dimensions
    };

    public GetSymbolInstanceAttributeListService(@Nullable String program) {
        super(program, SYMBOL_CLASS_ID, REQUESTED_ATTRIBUTES, new SymbolAttributesDecoder(program));
    }

    private static class SymbolAttributesDecoder implements AttributesDecoder<SymbolInstance> {

        private final String program;

        private SymbolAttributesDecoder(String program) {
            this.program = program;
        }

        @Override
        public SymbolInstance decode(int instanceId, ByteBuf buffer) {
            // attribute 1 - symbol name
            int nameLength = buffer.readUnsignedShort();
            String name = buffer.toString(
                buffer.readerIndex(),
                nameLength,
                StandardCharsets.US_ASCII
            );
            buffer.skipBytes(nameLength);

            // attribute 2 - symbol type
            int type = buffer.readUnsignedShort();

            // attribute 8 - dimensions
            int d1Size = buffer.readInt();
            int d2Size = buffer.readInt();
            int d3Size = buffer.readInt();

            return new SymbolInstance(program, name, instanceId, type, d1Size, d2Size, d3Size);
        }

    }

}
