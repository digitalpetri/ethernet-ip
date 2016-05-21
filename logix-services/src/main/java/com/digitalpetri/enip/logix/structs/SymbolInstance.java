package com.digitalpetri.enip.logix.structs;

import java.util.Optional;

public final class SymbolInstance {

    private final String program;
    private final String name;
    private final int instanceId;
    private final int type;
    private final int d1Size;
    private final int d2Size;
    private final int d3Size;

    public SymbolInstance(String program,
                          String name,
                          int instanceId,
                          int type,
                          int d1Size,
                          int d2Size,
                          int d3Size) {

        this.program = program;
        this.name = name;
        this.instanceId = instanceId;
        this.type = type;
        this.d1Size = d1Size;
        this.d2Size = d2Size;
        this.d3Size = d3Size;
    }

    public Optional<String> getProgram() {
        return Optional.ofNullable(program);
    }

    public String getName() {
        return name;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public int getType() {
        return type;
    }

    public int getD1Size() {
        return d1Size;
    }

    public int getD2Size() {
        return d2Size;
    }

    public int getD3Size() {
        return d3Size;
    }

    @Override
    public String toString() {
        return "SymbolInstance{" +
            "program=" + program +
            ", name='" + name + '\'' +
            ", instanceId=" + instanceId +
            ", type=" + type +
            '}';
    }

}
