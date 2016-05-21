package com.digitalpetri.enip.logix.structs;

import java.io.Serializable;

public final class TemplateAttributes implements Serializable {

    private final int handle;
    private final int memberCount;
    private final int objectDefinitionSize;
    private final int structureSize;

    public TemplateAttributes(int handle, int memberCount, int objectDefinitionSize, int structureSize) {
        this.handle = handle;
        this.memberCount = memberCount;
        this.objectDefinitionSize = objectDefinitionSize;
        this.structureSize = structureSize;
    }

    public int getHandle() {
        return handle;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public int getObjectDefinitionSize() {
        return objectDefinitionSize;
    }

    public int getStructureSize() {
        return structureSize;
    }

    @Override
    public String toString() {
        return "TemplateAttributes{" +
            "handle=" + handle +
            ", memberCount=" + memberCount +
            ", objectDefinitionSize=" + objectDefinitionSize +
            ", structureSize=" + structureSize +
            '}';
    }

}
