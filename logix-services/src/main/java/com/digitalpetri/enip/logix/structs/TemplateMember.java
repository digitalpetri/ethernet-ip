package com.digitalpetri.enip.logix.structs;

import java.io.Serializable;

public final class TemplateMember implements Serializable {

    private final String name;
    private final int infoWord;
    private final int symbolType;
    private final int offset;

    public TemplateMember(String name, int infoWord, int symbolType, int offset) {
        this.name = name;
        this.infoWord = infoWord;
        this.symbolType = symbolType;
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public int getInfoWord() {
        return infoWord;
    }

    public int getSymbolType() {
        return symbolType;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "TemplateMember{" +
            "name='" + name + '\'' +
            ", infoWord=" + infoWord +
            ", symbolType=" + symbolType +
            ", offset=" + offset +
            '}';
    }

}
