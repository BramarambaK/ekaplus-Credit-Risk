package com.eka.connect.creditrisk.constants;

public enum CreditLimitSourceEnum {

	CREDENDO("Credendo", 1), OWN_RISK("Own Risk", 2), NEXUS("Nexus", 3);

	private String limitSourceName;
	private int order;

	private CreditLimitSourceEnum(String limitSource, int order) {

		this.limitSourceName = limitSource;
		this.order = order;
	}

	public String getLimitSourceName() {
		return limitSourceName;
	}

	public int getOrder() {
		return order;
	}

	public static CreditLimitSourceEnum getCreditLimitSourceEnumByLimitSource(
			String limitSource) {

		switch ((limitSource)) {
		case "Credendo":
			return CreditLimitSourceEnum.CREDENDO;
		case "Own Risk":
			return CreditLimitSourceEnum.OWN_RISK;
		case "Nexus":
			return CreditLimitSourceEnum.NEXUS;
		default:
			return CreditLimitSourceEnum.OWN_RISK;
		}

	}
}
