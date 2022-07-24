package com.eka.connect.creditrisk.constants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Please do not change the order. Order is maintained here
 * 
 * @author rajeshks
 *
 */

public enum CreditLimitTypeGroupEnum {
	PRE_BOOKED(CreditLimitTypeEnum.CONTRACT_FULL_TERM,
			CreditLimitTypeEnum.CONTRACT_PARTIAL_TERM,
			CreditLimitTypeEnum.PRE_PAYMENT_FULL_TERM,CreditLimitTypeEnum.PRE_PAYMENT_PARTIAL_TERM,CreditLimitTypeEnum.TOP_UP_CONTRACT_FULL_TERM), TEMPORARY(
			CreditLimitTypeEnum.TEMPORARY), POOL(
			CreditLimitTypeEnum.CREDIT_LIMIT, CreditLimitTypeEnum.OWN_LIMIT,CreditLimitTypeEnum.TOP_UP_CREDIT_LIMIT,
			CreditLimitTypeEnum.PRE_PAYMENT_CREDIT_LIMIT,CreditLimitTypeEnum.PRE_PAYMENT_LIMIT);

	private static final Map<CreditLimitTypeEnum, CreditLimitTypeGroupEnum> nameIndex = new HashMap<>();
	static {
		for (CreditLimitTypeGroupEnum e : CreditLimitTypeGroupEnum.values()) {
			e.getLimitType().forEach(limitType -> {
				nameIndex.put(limitType, e);
			});
		}
	}

	private List<CreditLimitTypeEnum> limitType;

	private CreditLimitTypeGroupEnum(CreditLimitTypeEnum... limitType) {
		this.limitType = Arrays.asList(limitType);
	}

	public List<CreditLimitTypeEnum> getLimitType() {
		return limitType;
	}

	public static CreditLimitTypeGroupEnum getCreditLimitTypeGroupEnumByLimitType(
			CreditLimitTypeEnum limitType) {

		return nameIndex.get(limitType);

	}

	public static void main(String[] args) {
		BigDecimal b = new BigDecimal(3434.4557d).setScale(3,
				RoundingMode.HALF_UP);
		System.out.println(String.format(
				CreditRiskConstants.INSUFFICIENT_AMOUNT_MESSAGE,
				b.toPlainString()));
	}

	public static void main1(String[] args) throws Exception {
		String dateString = "2019-04-30T05:30:00.000+05:30";

		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
		/*
		 * LocalDateTime today = LocalDateTime.now(); String isoDateTom =
		 * today.plusDays(1).toString(); isoDateTom = isoDateTom.substring(0,
		 * isoDateTom.length() - 4); isoDateTom = isoDateTom + "+00:00";
		 */
		LocalDateTime ldt = LocalDateTime.parse(dateString, formatter);
		// System.out.println(ldt.toLocalDate().);

		System.out.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
				.parse("2019-04-25T05:30:00.000"));
		System.out.println("List values .....");
		List<String> list = new ArrayList<String>();
		list.add("1");
		list.add("2");
		list.add("1");
		list.add("4");
		list.add("3");

		for (String temp : list) {
			System.out.println(temp);
		}

		Set<String> set = new TreeSet<String>(list);

		System.out.println("Set values .....");
		for (String temp : set) {
			System.out.println(temp);
		}
		System.out.println("List1 values .....");
		List<String> list1 = new ArrayList<String>(set);

		for (String temp : list1) {
			System.out.println(temp);
		}
	}

}
