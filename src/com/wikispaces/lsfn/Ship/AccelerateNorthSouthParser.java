package com.wikispaces.lsfn.Ship;

import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Subscription.AccelerateNorthSouth;
import com.wikispaces.lsfn.Shared.Subscription.Subscribeable;
import com.wikispaces.lsfn.Shared.Subscription.SubscriptionMessageParser;
import com.wikispaces.lsfn.Shared.UnitDirection;

public class AccelerateNorthSouthParser extends SubscriptionMessageParser {

	protected AccelerateNorthSouthParser() {
		super(new AccelerateNorthSouth(UnitDirection.NOWHERE));
	}

	@Override
	public Subscribeable parse_subscription_update(Subscription_update s) throws PublishFailedException {
		return new AccelerateNorthSouth(UnitDirection.lookup(s.getInt32Value(), 0));
	}
}
