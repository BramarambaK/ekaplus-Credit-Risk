package com.eka.connect.creditrisk.executor;

import java.util.concurrent.Callable;

import com.eka.connect.creditrisk.dataobject.ItemResponse;
import com.eka.connect.creditrisk.util.MovementsCreditCheckCalculator;

public class MovementsCreditCheckCallable implements Callable<ItemResponse> {

	MovementsCreditCheckCalculator calculator = null;

	public MovementsCreditCheckCallable(
			MovementsCreditCheckCalculator calculator) {
		this.calculator = calculator;
	}

	@Override
	public ItemResponse call() throws Exception {
		return calculator.calculate();
	}
}

