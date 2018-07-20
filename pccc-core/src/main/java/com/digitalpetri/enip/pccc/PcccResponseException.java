package com.digitalpetri.enip.pccc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.digitalpetri.enip.cip.CipStatusCodes;

public class PcccResponseException extends Exception {
	private static final long serialVersionUID = 7250921816036988324L;

	private final int generalStatus;
	private final int[] additionalStatus;
	private final int pcccStatus;
	private final int extendedStatus;

	public PcccResponseException(int generalStatus, int[] additionalStatus) {
		this.generalStatus = generalStatus;
		this.additionalStatus = additionalStatus;
		this.pcccStatus = -1;
		this.extendedStatus = -1;
	}

	public PcccResponseException(int pcccStatus, int extendedStatus) {
		this.pcccStatus = pcccStatus;
		this.extendedStatus = extendedStatus;
		this.generalStatus = -1;
		this.additionalStatus = new int[] {};
	}

	public int getGeneralStatus() {
		return generalStatus;
	}

	public int[] getAdditionalStatus() {
		return additionalStatus;
	}

	public int getPcccStatus() {
		return pcccStatus;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();

		if (generalStatus > -1) {
			sb.append(String.format("generalStatus=0x%02X", generalStatus));
			CipStatusCodes.getName(generalStatus).ifPresent(name -> sb.append(" [").append(name).append("] "));

			List<String> as = Arrays.stream(additionalStatus).mapToObj(a -> String.format("0x%04X", a))
					.collect(Collectors.toList());

			if (!as.isEmpty()) {
				String additional = "[" + String.join(",", as) + "]";
				sb.append(", additional=").append(additional);
			}
		} else if (pcccStatus > -1) {
			sb.append(String.format("pcccStatus=0x%02X", pcccStatus));
			sb.append(" [").append(PcccStatusCodes.getName(pcccStatus)).append("] ");

			if (extendedStatus > -1) {
				sb.append(String.format(", extendedStatus=0x%02X", extendedStatus));
				sb.append(" [").append(PcccExtendedStatusCodes.getName(extendedStatus)).append("] ");
			}
		}

		return sb.toString();
	}

	public static Optional<PcccResponseException> extract(Throwable ex) {
		if (ex instanceof PcccResponseException) {
			return Optional.of((PcccResponseException) ex);
		} else {
			Throwable cause = ex.getCause();
			return cause != null ? extract(cause) : Optional.empty();
		}
	}
}
