package com.wikispaces.lsfn.Interface;


import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Subscription.AccelerateNorthSouth;
import com.wikispaces.lsfn.Shared.Subscription.Subscribeable;
import com.wikispaces.lsfn.Shared.Subscription.SubscriptionMessageBuilder;

public class AccelerateNorthSouthBuilder extends SubscriptionMessageBuilder {
	protected AccelerateNorthSouthBuilder() {
		super(new AccelerateNorthSouth(UnitDirection.NOWHERE));
	}

	public Subscription_update build_subscription_update(Subscribeable s) {
		AccelerateNorthSouth a = (AccelerateNorthSouth)s;
		
		return Subscription_update.newBuilder()
				.setID(a.get_id())
				.setInt32Value(a.get_value())
				.build();
	}
}
