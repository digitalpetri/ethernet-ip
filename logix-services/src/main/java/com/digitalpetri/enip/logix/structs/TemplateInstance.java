package com.digitalpetri.enip.logix.structs;

import java.io.Serializable;
import java.util.List;

public final class TemplateInstance implements Serializable {

    private final String name;
    private final int symbolType;
    private final TemplateAttributes attributes;
    private final List<TemplateMember> members;

    public TemplateInstance(String name,
                            int symbolType,
                            TemplateAttributes attributes,
                            List<TemplateMember> members) {

        this.name = name;
        this.symbolType = symbolType;
        this.attributes = attributes;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public int getSymbolType() {
        return symbolType;
    }

    public TemplateAttributes getAttributes() {
        return attributes;
    }

    public List<TemplateMember> getMembers() {
        return members;
    }

    public int getInstanceId() {
        return symbolType & 0x0FFF;
    }

    @Override
    public String toString() {
        return "TemplateInstance{" +
            "name='" + name + '\'' +
            ", attributes=" + attributes +
            ", members=" + members +
            '}';
    }

}
