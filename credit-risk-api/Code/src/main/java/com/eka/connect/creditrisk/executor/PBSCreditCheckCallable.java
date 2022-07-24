package com.eka.connect.creditrisk.executor;

import java.util.concurrent.Callable;

import com.eka.connect.creditrisk.dataobject.ItemResponse;
import com.eka.connect.creditrisk.util.PBSCreditCheckCalculator;


public class PBSCreditCheckCallable implements Callable<ItemResponse> {

	PBSCreditCheckCalculator calculator = null;

	public PBSCreditCheckCallable(
			PBSCreditCheckCalculator calculator) {
		this.calculator = calculator;
	}

	@Override
	public ItemResponse call() throws Exception {
		return calculator.calculate();
	}
}
