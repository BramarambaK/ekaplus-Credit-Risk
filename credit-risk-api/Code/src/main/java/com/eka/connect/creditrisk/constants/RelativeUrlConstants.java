package com.eka.connect.creditrisk.constants;

public interface RelativeUrlConstants {

	String CONNECT_PROPERTY_URL="/property/";
	String CONNECT_VALIDATE_TOKEN = "/api/userinfo"; 
	String CONNECT_GET_ALL_PROPERTIES_BY_APP = "/property/{refTypeId}/list";
	String PLATFORM_COLLECTION_URL ="/collection/v1?collectionName=${collectionName}&page=1&start=0&limit=100000";
	String CONNECT_PLATFORM_COLLECTION_URL = "/collectionmapper/"
			+ CreditRiskConstants.CREDIT_RISK_APP_REF_TYPE_ID
			+ "/01abf4cd-c682-4988-9b8c-c84014a230e7/fetchCollectionRecords";
	String CONNECT_WORKFLOW_GET_DATA = "/workflow/data";
	//String CONNECT_WORKFLOW_COUNTERPARTY_DATA_URL   = "/workflow/"+CreditRiskConstants.CREDIT_RISK_APP_REF_TYPE_ID+"/counterpartylist/data";
	//String CONNECT_WORKFLOW_LIMITMAINTENANCE_DATA_URL = "/workflow/"+CreditRiskConstants.CREDIT_RISK_APP_REF_TYPE_ID+"/limitlist/data";
	//String CONNECT_WORKFLOW_CREDITSTOP_ELIGIBILITY_DATA_URL = "/workflow/"+CreditRiskConstants.CREDIT_RISK_APP_REF_TYPE_ID+"/creditstoplist/data";
	String CONNECT_WORKFLOW_SAVE ="/workflow";
	String MDM_DATA_URL="/mdm/"+CreditRiskConstants.CREDIT_RISK_APP_REF_TYPE_ID+"/data";
}
