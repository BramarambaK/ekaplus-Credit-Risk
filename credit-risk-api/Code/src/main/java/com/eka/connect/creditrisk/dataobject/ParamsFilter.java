package com.eka.connect.creditrisk.dataobject;

public class ParamsFilter {
	
	private int from = 0;
	private int size  = 50;

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	private ElasticQueryFilter query;

	public ElasticQueryFilter getQuery() {
		return query;
	}

	public void setQuery(ElasticQueryFilter query) {
		this.query = query;
	}

}
