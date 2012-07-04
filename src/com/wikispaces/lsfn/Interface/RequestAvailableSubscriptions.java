package com.wikispaces.lsfn.Interface;

import com.wikispaces.lsfn.Shared.LSFN.IS;

public class RequestAvailableSubscriptions {
	public IS build_message() {
		return IS.newBuilder()
			.setAvailableSubscriptionsList(true)
            .build();
	}
}
