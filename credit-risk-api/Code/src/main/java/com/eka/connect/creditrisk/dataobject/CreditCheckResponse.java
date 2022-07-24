package com.eka.connect.creditrisk.dataobject;

import java.util.List;

public class CreditCheckResponse {

	
	private String status;
	
	 private String requestRefNo;
	 
	 private List<ItemResponse> responseList;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRequestRefNo() {
		return requestRefNo;
	}

	public void setRequestRefNo(String requestRefNo) {
		this.requestRefNo = requestRefNo;
	}

	public List<ItemResponse> getResponseList() {
		return responseList;
	}

	public void setResponseList(List<ItemResponse> responseList) {
		this.responseList = responseList;
	}
	 
	 
}
