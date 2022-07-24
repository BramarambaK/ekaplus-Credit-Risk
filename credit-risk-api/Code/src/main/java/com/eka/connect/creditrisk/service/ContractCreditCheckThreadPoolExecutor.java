package com.eka.connect.creditrisk.service;

import java.util.concurrent.Callable;

import com.eka.connect.creditrisk.util.ContractCreditCheckCalculator;

/**
 * 
 * @author rajeshks
 *
 */
public class ContractCreditCheckThreadPoolExecutor {

	public ContractCreditCheckCalculator populateCalculatorObjectForCreditCheck() {

		ContractCreditCheckCalculator  ccc = new ContractCreditCheckCalculator();
		
		
		
		
		 
		
		
		
		/*
		 * ContractCreditCheckCalculator contractCalculator = new
		 * ContractCreditCheckCalculator( c, limitMaintenanceDetails,
		 * tccrDetails);
		 * contractCalculator.setCounterPartyExposure(counterpartyExposures);
		 * contractCalculator.setDerivedPaymentTerms(map);
		 * contractCalculator.setFxRatesMap(fxRatesMap);
		 */

		return null;

	}
	
}
	class ContractCreditCheckCallable implements Callable<Void>{
	
	ContractCreditCheckCalculator cccc = null;
	public ContractCreditCheckCallable(ContractCreditCheckCalculator cccc){
		
		this.cccc = cccc;	
	}
	
	

		@Override
		public Void call() throws Exception {
			 
		return null;	
			
		}
		
		
	}

