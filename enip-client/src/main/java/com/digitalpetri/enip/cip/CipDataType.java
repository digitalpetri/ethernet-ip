package com.digitalpetri.enip.cip;

public interface CipDataType {

    int getCode();

    boolean isStructured();

    public static CipDataType atomic(int code) {
        return new CipDataType() {
            @Override
            public int getCode() {
                return code;
            }

            @Override
            public boolean isStructured() {
                return false;
            }
        };
    }

    public static CipDataType structured(int code) {
        return new CipDataType() {
            @Override
            public int getCode() {
                return code;
            }

            @Override
            public boolean isStructured() {
                return true;
            }
        };
    }

}
