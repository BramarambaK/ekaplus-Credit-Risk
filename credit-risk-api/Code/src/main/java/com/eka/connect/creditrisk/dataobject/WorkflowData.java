package com.eka.connect.creditrisk.dataobject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WorkflowData {

	private String appId;
	private String workFlowTask;
	private FilterData filterData;
	private ParamsFilter qP;

	@JsonProperty("qP")
	public ParamsFilter getQp() {
		return qP;
	}
	@JsonProperty("qP")
	public void setQp(ParamsFilter qP) {
		this.qP = qP;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getWorkFlowTask() {
		return workFlowTask;
	}

	public void setWorkFlowTask(String workFlowTask) {
		this.workFlowTask = workFlowTask;
	}

	public FilterData getFilterData() {
		return filterData;
	}

	public void setFilterData(FilterData filterData) {
		this.filterData = filterData;
	}

	@Override
	public String toString() {

		try {
			return new ObjectMapper().writerWithDefaultPrettyPrinter()
					.writeValueAsString(this);
		} catch (JsonProcessingException e) {

		}
		return super.toString();

	}
	
	@SuppressWarnings("unchecked")
	public void convertFilterToElasticFilter() {
		if (filterData != null) {
			ParamsFilter params = new ParamsFilter();
			params.setFrom(0);
			params.setSize(50);
			this.setQp(params);
			ElasticQueryFilter query = new ElasticQueryFilter();
			params.setQuery(query);
			BooleanFilter bool = new BooleanFilter();
			query.setBool(bool);
			List<MustFilter> mustList = new ArrayList<>();
			bool.setMust(mustList);
			for (MongoOperations filter : filterData.getFilter()) {

				MustFilter mustFilter = new MustFilter();
				mustList.add(mustFilter);
				
				Map<String, List<Object>> terms = new HashMap<>();
				mustFilter.setTerms(terms);
				Object value = filter.getValue();
				List<Object> values = new ArrayList<>();

				switch (filter.getOperator()) {
				case "eq":
				case "in":

					if (value instanceof String) {
						values.add(value);
					} else if (value instanceof String[]) {
						values.addAll(Arrays.asList((String[])value));
					} else if (value instanceof List) {
						values.addAll((List) value);
					}
					terms.put(filter.getFieldName() + ".raw", values);
					break;
				default:
					terms.put(filter.getFieldName()+".raw", values);

				} 

			}

		}

	}

	public void resetFilter() {
		this.filterData = null;
	}
	
	 
}
