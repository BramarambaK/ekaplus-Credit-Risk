package com.eka.connect.creditrisk.dataobject;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
//Need to populate this Pojo to save Data via workflow
@JsonInclude(Include.NON_NULL)
public class WorkflowPojo {
	private String task;
	private String workflowTaskName;
	private String appId;
	private String id;
	private Map<String,Object> output = new HashMap<>();
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	public String getWorkflowTaskName() {
		return workflowTaskName;
	}
	public void setWorkflowTaskName(String workflowTaskName) {
		this.workflowTaskName = workflowTaskName;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Map<String, Object> getOutput() {
		return output;
	}
	public void setOutput(Map<String, Object> output) {
		this.output = output;
	}

}
