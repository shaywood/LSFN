package com.wikispaces.lsfn.Ship;

import java.util.Random;

import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Subscribeable;

public class TestBuilder extends SubscriptionMessageBuilder {
	public TestBuilder() {
		super(Subscribeable.TEST);
	}
	
	@Override
	public Subscription_update build_subscription_update() {
		return Subscription_update.newBuilder()
			.setID(subscribeable.get_id())
			.setStringValue(Subscribeable.TEST_MESSAGE)
			.build();
	}

	@Override
	public boolean has_updated() {
		return new Random().nextInt(1000) % 1000 == 0;
	}
}
