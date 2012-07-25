package com.wikispaces.lsfn.Interface;

import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Subscription.Accelerate;
import com.wikispaces.lsfn.Shared.Subscription.Subscribeable;
import com.wikispaces.lsfn.Shared.Subscription.SubscriptionMessageBuilder;

public class AccelerateBuilder extends SubscriptionMessageBuilder {
	protected AccelerateBuilder() {
		super(new Accelerate(UnitDirection.NOWHERE));
	}

	public Subscription_update build_subscription_update(Subscribeable s) {
		Accelerate a = (Accelerate)s;
		
		return Subscription_update.newBuilder()
				.setID(a.get_id())
				.addInt32Value(a.get_direction().get_north_south())
				.addInt32Value(a.get_direction().get_east_west())
				.build();
	}
}