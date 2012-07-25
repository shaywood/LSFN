package com.wikispaces.lsfn.Ship;

import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Subscription.Accelerate;
import com.wikispaces.lsfn.Shared.Subscription.Subscribeable;
import com.wikispaces.lsfn.Shared.Subscription.SubscriptionMessageParser;

public class AccelerateParser extends SubscriptionMessageParser {

	protected AccelerateParser() {
		super(new Accelerate(UnitDirection.NOWHERE));
	}

	@Override
	public Subscribeable parse_subscription_update(Subscription_update s) throws PublishFailedException {
		return new Accelerate(UnitDirection.lookup(s.getInt32Value(0), s.getInt32Value(1)));
	}
}