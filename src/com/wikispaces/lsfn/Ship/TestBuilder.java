package com.wikispaces.lsfn.Ship;


import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Subscription.Subscribeable;
import com.wikispaces.lsfn.Shared.Subscription.SubscriptionMessageBuilder;

public class TestBuilder extends SubscriptionMessageBuilder {
	public TestBuilder() {
		super(new com.wikispaces.lsfn.Shared.Subscription.Test());
	}
	
	@Override
	public Subscription_update build_subscription_update(Subscribeable s) {
		com.wikispaces.lsfn.Shared.Subscription.Test t = (com.wikispaces.lsfn.Shared.Subscription.Test)s;
		return Subscription_update.newBuilder()
				.setID(t.get_id())
				.setStringValue(0, t.get_test_message())
				.build();
	}
}
