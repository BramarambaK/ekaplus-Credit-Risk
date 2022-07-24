package com.eka.connect.creditrisk.util;

import java.util.Comparator;

import com.eka.connect.creditrisk.constants.CreditLimitSourceEnum;
import com.eka.connect.creditrisk.constants.CreditLimitTypeEnum;
import com.eka.connect.creditrisk.dataobject.LimitMaintenanceDetails;

public class LimitMaintenanceComparator implements Comparator<LimitMaintenanceDetails> {

	@Override
	public int compare(LimitMaintenanceDetails o1, LimitMaintenanceDetails o2) {
		// TODO Auto-generated method stub
		int sourceLimitSourceInt = CreditLimitSourceEnum
				.getCreditLimitSourceEnumByLimitSource(
						o1.getCreditLimitSourceDisplayName()).getOrder();
		int destLimitSourceInt = CreditLimitSourceEnum
				.getCreditLimitSourceEnumByLimitSource(
						o2.getCreditLimitSourceDisplayName()).getOrder();

		int result = LimitMaintenanceComparator.compareInt(
				sourceLimitSourceInt, destLimitSourceInt);
		if (result != 0) {
			return result;
		} else {

			int sourceLimitTypeInt = CreditLimitTypeEnum.getEnumByName(
					o1.getCreditLimitTypeDisplayName()).getOrder();
			int destLimitTypeInt = CreditLimitTypeEnum.getEnumByName(
					o2.getCreditLimitTypeDisplayName()).getOrder();
			result = LimitMaintenanceComparator.compareInt(sourceLimitTypeInt,
					destLimitTypeInt);
			if (result != 0) {
				return result;
			} else {
				return LimitMaintenanceComparator.compareInt(
						o1.getChartingOrder(), o2.getChartingOrder());
			}

		}
	}

	public static int compareInt(int x, int y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

}