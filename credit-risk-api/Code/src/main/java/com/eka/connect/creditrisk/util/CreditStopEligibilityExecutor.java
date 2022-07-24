package com.eka.connect.creditrisk.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.dataobject.CounterPartyDetails;
import com.eka.connect.creditrisk.dataobject.CreditStopEligibility;

public class CreditStopEligibilityExecutor {

	private List<CreditStopEligibility> creditStopData;
	private List<CounterPartyDetails> counterpartyData;
	private Map<String, List<CreditStopEligibility>> creditStopMap = null;
	private Map<String, CounterPartyDetails> counterPartyMap = null;
	private Map<String, List<CounterPartyDetails>> counterPartyGroupMap = null;
	private String appRefTypeId;
	
	public CreditStopEligibilityExecutor() {
		// TODO Auto-generated constructor stub
	}
	
	public CreditStopEligibilityExecutor(List<CreditStopEligibility> creditStopData,
			List<CounterPartyDetails> counterpartyData,String appRefTypeId){
		super();
		this.counterpartyData = counterpartyData;
		this.creditStopData = creditStopData;
		this.appRefTypeId = appRefTypeId;
	}
	
	public void execute(){
		if (!CollectionUtils.isEmpty(creditStopData)
				&& !CollectionUtils.isEmpty(counterpartyData)) {
			// Group credit stop eligibility data into 3 buckets. i.e automatic suspension, prepayment stop,delivery stop
			creditStopMap = getGroupedELigibilityData(creditStopData);

			counterPartyMap = new HashMap<>();
			
			Map<String, List<CounterPartyDetails>> collect = counterpartyData
					.stream()
					.collect(Collectors.groupingBy(CounterPartyDetails::getCounterpartyName)); 
			groupCounterPartyByCounterpartyGroup(counterpartyData);
			if(collect!=null){
			/*	 Iterator<Entry<String, List<CounterPartyDetails>>> iterator = collect.entrySet().iterator();
				while(iterator.hasNext()){
					Entry<String, List<CounterPartyDetails>> next = iterator.next();
					if(next.getValue()!=null)
					counterPartyMap.put(next.getKey(), next.getValue().get(0));
				}*/
				collect.forEach((k,v)->{
					counterPartyMap.put(k, v.get(0));
				});
			}
			
			processCreditStopRecords(CreditRiskConstants.AUTOMATIC_SUSPENSION);
			processCreditStopRecords(CreditRiskConstants.PRE_PAYMENT_STOP);
			processCreditStopRecords(CreditRiskConstants.PRE_PAYMENT_STOP);
			processRevokingDeliveryStop(); 
			

		}
	}

	


	private Map<String, List<CounterPartyDetails>> groupCounterPartyByCounterpartyGroup(
			List<CounterPartyDetails> counterpartyData2) {
		counterPartyGroupMap = counterpartyData
				.stream().filter(e-> e.getCreditLimitLevel()!=null && "Counterparty Group".equalsIgnoreCase(e.getCreditLimitLevel()))
				.collect(
						Collectors
								.groupingBy(CounterPartyDetails::getCounterpartyGroup));
		return counterPartyGroupMap;
	}

	private void processRevokingDeliveryStop() {
		List<CounterPartyDetails> list = counterpartyData
				.stream()
				.filter(e -> e.getCreditStopStatus() == null
						&& e.getCreditRiskStatusDisplayName() != null
						&& CreditRiskConstants.DELIVERY_STOP.equalsIgnoreCase(e
								.getCreditRiskStatusDisplayName()))
				.collect(Collectors.toList());
		{

			if (list != null)
				for (int i = 0; i < list.size(); i++) {
					list.get(i).setCreditStopStatus(CreditRiskConstants.ACTIVE);
				}
		}

	}

	private void processCreditStopRecords(String bucketKey) {
		
		List<CreditStopEligibility> list = creditStopMap.get(bucketKey);
		Set<String> counterPartyNames  = new HashSet<>();
		for(CreditStopEligibility cse:list){
			counterPartyNames.add(cse.getCounterparty());
		}
		for (String counterPartyName : counterPartyNames) {
			CounterPartyDetails counterPartyDetails = counterPartyMap
					.get(counterPartyName);
			setCounterPartyCreditStopStatus(counterPartyDetails,bucketKey);
		}
	}

	private void setCounterPartyCreditStopStatus(
			CounterPartyDetails counterPartyDetails, String newStatus) {
		if (counterPartyDetails != null) {
			counterPartyDetails
					.setCreditStopStatus(newStatus);
			if (counterPartyDetails.getCounterpartyGroup() != null
					&& counterPartyGroupMap.containsKey(counterPartyDetails
							.getCounterpartyGroup())) {
				for (CounterPartyDetails c : counterPartyGroupMap
						.get(counterPartyDetails.getCounterpartyGroup())) {
					if(c.getCreditStopStatus()==null)
					c.setCreditStopStatus(newStatus);
				}
			}
		}
	}

	private Map<String, List<CreditStopEligibility>> getGroupedELigibilityData(
			List<CreditStopEligibility> eligibilityData) {
		Map<String, List<CreditStopEligibility>> creditStopMap;
		creditStopMap = new HashMap<>();
		if (!CollectionUtils.isEmpty(eligibilityData)) {
			eligibilityData = eligibilityData.stream()
					.filter(e -> e.isEligible()).collect(Collectors.toList());
			creditStopMap.put(
					CreditRiskConstants.AUTOMATIC_SUSPENSION,
					eligibilityData.stream()
							.filter(e -> e.isEligibleForAutomaticSuspension())
							.collect(Collectors.toList()));

			creditStopMap.put(
					CreditRiskConstants.PRE_PAYMENT_STOP,
					eligibilityData.stream()
							.filter(e -> e.isEligibleForPrepaymentStop())
							.collect(Collectors.toList()));

			creditStopMap.put(
					CreditRiskConstants.DELIVERY_STOP,
					eligibilityData.stream()
							.filter(e -> e.isEligibleForDeliveryStop())
							.collect(Collectors.toList()));
		}

		return creditStopMap;
	}
	public List<CreditStopEligibility> getCreditStopData() {
		return creditStopData;
	}

	public void setCreditStopData(List<CreditStopEligibility> creditStopData) {
		this.creditStopData = creditStopData;
	}

	public List<CounterPartyDetails> getCounterpartyData() {
		return counterpartyData;
	}

	public void setCounterpartyData(List<CounterPartyDetails> counterpartyData) {
		this.counterpartyData = counterpartyData;
	}

	public Map<String, List<CreditStopEligibility>> getCreditStopMap() {
		return creditStopMap;
	}

	public void setCreditStopMap(
			Map<String, List<CreditStopEligibility>> creditStopMap) {
		this.creditStopMap = creditStopMap;
	}

	public Map<String, CounterPartyDetails> getCounterPartyMap() {
		return counterPartyMap;
	}

	public void setCounterPartyMap(
			Map<String,CounterPartyDetails> counterPartyMap) {
		this.counterPartyMap = counterPartyMap;
	}

	public Map<String, List<CounterPartyDetails>> getCounterPartyGroupMap() {
		return counterPartyGroupMap;
	}

	public void setCounterPartyGroupMap(
			Map<String, List<CounterPartyDetails>> counterPartyGroupMap) {
		this.counterPartyGroupMap = counterPartyGroupMap;
	}
}
