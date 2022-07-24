package com.eka.connect.creditrisk.dataobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ItemResponse {
	
	private String status;
	private String counterParty;
	private String counterPartyGroup;
	private String description;
	private String blockType;
	public String getCounterParty() {
		return counterParty;
	}
	public void setCounterParty(String counterParty) {
		this.counterParty = counterParty;
	}
	public String getCounterPartyGroup() {
		return counterPartyGroup;
	}
	public void setCounterPartyGroup(String counterPartyGroup) {
		this.counterPartyGroup = counterPartyGroup;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getBlockType() {
		return blockType;
	}
	public void setBlockType(String blockType) {
		this.blockType = blockType;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
		} catch (JsonProcessingException e) {

		}
		return this.toString();
	}
}
