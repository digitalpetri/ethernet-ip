package com.digitalpetri.enip.logix.structs;

import java.io.Serializable;
import java.util.List;

public final class TemplateInstance implements Serializable {

    private final String name;
    private final int instanceId;
    private final TemplateAttributes attributes;
    private final List<TemplateMember> members;

    public TemplateInstance(String name,
                            int instanceId,
                            TemplateAttributes attributes,
                            List<TemplateMember> members) {

        this.name = name;
        this.instanceId = instanceId;
        this.attributes = attributes;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public TemplateAttributes getAttributes() {
        return attributes;
    }

    public List<TemplateMember> getMembers() {
        return members;
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
