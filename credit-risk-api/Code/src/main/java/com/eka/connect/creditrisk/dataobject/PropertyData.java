package com.eka.connect.creditrisk.dataobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**

**/

@JsonIgnoreProperties(ignoreUnknown=true)
public class PropertyData {

	private String propertyLevel;
	private String type;
	private String propertyValue;
	private String propertyName;
	private String refTypeId;
	
	public String getPropertyLevel() {
		return propertyLevel;
	}
	public void setPropertyLevel(String propertyLevel) {
		this.propertyLevel = propertyLevel;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPropertyValue() {
		return propertyValue;
	}
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public String getRefTypeId() {
		return refTypeId;
	}
	public void setRefTypeId(String refTypeId) {
		this.refTypeId = refTypeId;
	}
	
	
	
}

