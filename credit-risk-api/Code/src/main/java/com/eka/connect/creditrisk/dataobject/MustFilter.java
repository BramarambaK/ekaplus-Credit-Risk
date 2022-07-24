package com.eka.connect.creditrisk.dataobject;

import java.util.List;
import java.util.Map;

public class MustFilter {

	Map<String,List<Object>> terms;

	public Map<String, List<Object>> getTerms() {
		return terms;
	}

	public void setTerms(Map<String, List<Object>> terms) {
		this.terms = terms;
	}
}
