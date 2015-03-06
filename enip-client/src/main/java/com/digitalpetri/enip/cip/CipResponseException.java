package com.digitalpetri.enip.cip;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CipResponseException extends Exception {

    private final int generalStatus;
    private final int[] additionalStatus;

    public CipResponseException(int generalStatus, int[] additionalStatus) {
        this.generalStatus = generalStatus;
        this.additionalStatus = additionalStatus;
    }

    public int getGeneralStatus() {
        return generalStatus;
    }

    public int[] getAdditionalStatus() {
        return additionalStatus;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("status=0x%02X", generalStatus));

        List<String> as = Arrays.stream(additionalStatus)
                .mapToObj(a -> String.format("0x%04X", a))
                .collect(Collectors.toList());

        String additional = "[" + String.join(",", as) + "]";

        sb.append(", additional=").append(additional);

        return sb.toString();
    }

}
