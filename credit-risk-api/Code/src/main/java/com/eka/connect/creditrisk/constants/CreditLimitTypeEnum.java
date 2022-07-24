package com.eka.connect.creditrisk.constants;

import java.util.HashMap;
import java.util.Map;

public enum CreditLimitTypeEnum {
	CONTRACT_FULL_TERM(CreditRiskConstants.CONTRACT_FULL_TERM, 1), CONTRACT_PARTIAL_TERM(
			CreditRiskConstants.CONTRACT_PARTIAL_TERM, 2), TEMPORARY(
			CreditRiskConstants.TEMPORARY, 3), CREDIT_LIMIT(
			CreditRiskConstants.CREDIT_LIMIT, 4), OWN_LIMIT(
					CreditRiskConstants.OWN_LIMIT, 5),TOP_UP_CONTRACT_FULL_TERM(
			CreditRiskConstants.TOP_UP_CONTRACT_FULL_TERM, 6),TOP_UP_CREDIT_LIMIT(
					CreditRiskConstants.TOP_UP_CREDIT_LIMIT, 7), PRE_PAYMENT_FULL_TERM(
			CreditRiskConstants.PRE_PAYMENT_FULL_TERM, 8), PRE_PAYMENT_PARTIAL_TERM(
			CreditRiskConstants.PRE_PAYMENT_PARTIAL_TERM, 9),PRE_PAYMENT_CREDIT_LIMIT(
					CreditRiskConstants.PRE_PAYMNET_CREDIT_LIMIT, 10),
					PRE_PAYMENT_LIMIT(CreditRiskConstants.PRE_PAYMENT_LIMIT,11);


	private int order;
	private String name;

	private static final Map<String, CreditLimitTypeEnum> nameIndex = new HashMap<>();
	static {
		for (CreditLimitTypeEnum e : CreditLimitTypeEnum.values()) {
			nameIndex.put(e.getName(), e);
		}
	}

	private CreditLimitTypeEnum(String name, int order) {
		this.name = name;
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public String getName() {
		return name;
	}

	public static CreditLimitTypeEnum getEnumByName(String name) {
		return nameIndex.get(name);
	}

} 


