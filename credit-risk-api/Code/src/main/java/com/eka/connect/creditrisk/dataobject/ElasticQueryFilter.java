package com.eka.connect.creditrisk.dataobject;


/**
  
  
    "query": {
        "bool": {
            "must": [
                {
                    "terms": {
                        "crmStatus.keyword": [
                            "Active"
                        ]
                    }
                }
            ],
            "must_not": [
                {
                    "exists": {
                        "field": "d"
                    }
                }
            ]
        }
    }
  
  
 *
 */
public class ElasticQueryFilter {
	
	
	private BooleanFilter bool;

	public BooleanFilter getBool() {
		return bool;
	}

	public void setBool(BooleanFilter bool) {
		this.bool = bool;
	}
	

}
