package com.wikispaces.lsfn.Interface;

import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Subscription.AccelerateEastWest;
import com.wikispaces.lsfn.Shared.Subscription.Subscribeable;
import com.wikispaces.lsfn.Shared.Subscription.SubscriptionMessageBuilder;

public class AccelerateEastWestBuilder extends SubscriptionMessageBuilder {
	protected AccelerateEastWestBuilder() {
		super(new AccelerateEastWest(UnitDirection.NOWHERE));
	}

	public Subscription_update build_subscription_update(Subscribeable s) {
		AccelerateEastWest a = (AccelerateEastWest)s;
		
		return Subscription_update.newBuilder()
				.setID(a.get_id())
				.setInt32Value(a.get_value())
				.build();
	}
}