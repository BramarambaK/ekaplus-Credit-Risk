package com.eka.connect.creditrisk.constants;

public enum ConditionEnum {

	regex("regex"), eq("eq"), ne("ne"), gt("gt"), gte("gte"), lt("lt"), lte(
			"lte"), in("in"), nin("nin"), exists("exists"), size("size"), or(
			"or"), like("like");

	private final String enumValue;

	ConditionEnum(String enumvalue) {
		this.enumValue = enumvalue;
	};

	public String getEnumValue() {
		return enumValue;
	}
}
