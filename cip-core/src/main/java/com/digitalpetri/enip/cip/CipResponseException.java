package com.digitalpetri.enip.cip;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

        CipStatusCodes.getName(generalStatus).ifPresent(name -> sb.append(" [").append(name).append("] "));

        List<String> as = Arrays.stream(additionalStatus)
            .mapToObj(a -> String.format("0x%04X", a))
            .collect(Collectors.toList());

        String additional = "[" + String.join(",", as) + "]";

        sb.append(", additional=").append(additional);

        return sb.toString();
    }

    /**
     * If {@code ex} is a {@link CipResponseException}, or if a {@link CipResponseException} can be found by walking
     * the exception cause chain, return it.
     *
     * @param ex the {@link Throwable} to extract from.
     * @return a {@link CipResponseException} if one was present in the exception chain.
     */
    public static Optional<CipResponseException> extract(Throwable ex) {
        if (ex instanceof CipResponseException) {
            return Optional.of((CipResponseException) ex);
        } else {
            Throwable cause = ex.getCause();
            return cause != null ?
                extract(cause) : Optional.empty();
        }
    }

}
