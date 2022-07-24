package com.eka.connect.creditrisk.dataobject;

import java.util.List;

public class BooleanFilter {
	
	private List<MustFilter> must;

	public List<MustFilter> getMust() {
		return must;
	}

	public void setMust(List<MustFilter> must) {
		this.must = must;
	}

}
