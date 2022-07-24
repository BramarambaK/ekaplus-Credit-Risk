package com.eka.connect.creditrisk.constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum DerivedPaymentTermsEnum {
	PP(PaymentTermConstants._58_Days, PaymentTermConstants._57_Days,
			PaymentTermConstants._56_Days, PaymentTermConstants._55_Days,
			PaymentTermConstants._52_Days, PaymentTermConstants._50_Days,
			PaymentTermConstants._49_Days, PaymentTermConstants._48_Days,
			PaymentTermConstants._47_Days, PaymentTermConstants._46_Days,
			PaymentTermConstants._45_Days, PaymentTermConstants._44_Days,
			PaymentTermConstants._41_Days, PaymentTermConstants._32_Days,
			PaymentTermConstants._31_Days), LC(PaymentTermConstants._30_Days,
			PaymentTermConstants._22_Days, PaymentTermConstants._21_Days,
			PaymentTermConstants._15_Days, PaymentTermConstants._14_Days,
			PaymentTermConstants._11_Days, PaymentTermConstants._9_Days,
			PaymentTermConstants._8_Days, PaymentTermConstants._7_Days,
			PaymentTermConstants._6_Days, PaymentTermConstants._5_Days,
			PaymentTermConstants._4_Days, PaymentTermConstants._3_Days,
			PaymentTermConstants._2_Days, PaymentTermConstants._0_Days,
			PaymentTermConstants._25_Days, PaymentTermConstants._90_Days,
			PaymentTermConstants._130_Days), OTHER(PaymentTermConstants._FOB,
			PaymentTermConstants._CIF, PaymentTermConstants._CAD,
			PaymentTermConstants._366_Days, PaymentTermConstants._365_Days,
			PaymentTermConstants._181_Days, PaymentTermConstants._76_Days,
			PaymentTermConstants._75_Days, PaymentTermConstants._74_Days,
			PaymentTermConstants._72_Days, PaymentTermConstants._71_Days,
			PaymentTermConstants._70_Days, PaymentTermConstants._69_Days,
			PaymentTermConstants._67_Days, PaymentTermConstants._66_Days,
			PaymentTermConstants._65_Days, PaymentTermConstants._64_Days,
			PaymentTermConstants._63_Days, PaymentTermConstants._62_Days,
			PaymentTermConstants._60_Days, PaymentTermConstants._59_Days,
			PaymentTermConstants._180);

	private static final Map<String, String> nameIndex = new HashMap<>();
	static {
		for (DerivedPaymentTermsEnum e : DerivedPaymentTermsEnum.values()) {
			Arrays.asList(e.paymentTerms).forEach(paymentTerm -> {
				nameIndex.put(paymentTerm, e.name());
			});
			
		}
	}
	
	public static String getDerivedPaymentTerm(
			String paymentTerm) {

		return nameIndex.get(paymentTerm);

	}


	private String[] paymentTerms;

	private DerivedPaymentTermsEnum(String... name) {
		this.paymentTerms = name;
	}
}
