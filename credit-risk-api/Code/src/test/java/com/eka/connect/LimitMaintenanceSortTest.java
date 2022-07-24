package com.eka.connect;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import com.eka.connect.creditrisk.dataobject.ContractCreditCheckRequest;

public class LimitMaintenanceSortTest {

	private static final Logger LOGGER = ESAPI
			.getLogger(LimitMaintenanceSortTest.class);

	public static void main1(String[] args) {
		String s = " ";
		System.out.println(s.charAt(0));

		// String s ="amends";
		System.out.println(s.matches("create|modify|amend"));
	}

	public static void main3(String[] args) {

		List<LinkedHashMap> list = new ArrayList<>();
		LinkedHashMap<String, Object> m = new LinkedHashMap<>();
		m.put("one", new Date());
		m.put("two", "two");
		list.add(m);

		JSONArray ar = new JSONArray(list);

		System.out.println(ar.toString());
	}

	public static void main2(String[] args) {

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("key1", "value1");
		jsonObj.put("key2", "value2");
		jsonObj.put("key3", 234);
		jsonObj.put("key4", "value4");

		String jsonString = jsonObj.toString();
		System.out.println(jsonString);
		String[] split = jsonString.split("key2");
		String joinedString = String.join("keyc", split);

		split = joinedString.split("key3");
		joinedString = String.join("keyd", split);
		System.out.println(joinedString);

	}

	public static void main(String[] args) {
		String key =  "failed";
	System.out.println(key.matches("(?-i)Failed"));
	}

	public static void main6(String[] args) {

		BigDecimal no = new BigDecimal(12475001098.120);
		Locale l = new Locale("en", "US");
		NumberFormat nf = NumberFormat.getNumberInstance(l);
		DecimalFormat df = (DecimalFormat) nf;
		System.out.println(df.format(no));
		l = new Locale("de", "");
		nf = NumberFormat.getNumberInstance(l);
		df = (DecimalFormat) nf;

		System.out.println(l.toString());
		System.out.println(l.toLanguageTag());
		System.out.println(df.format(no));
	}
}
