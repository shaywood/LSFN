package com.wikispaces.lsfn.Ship;

import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Subscription.AccelerateEastWest;
import com.wikispaces.lsfn.Shared.Subscription.Subscribeable;
import com.wikispaces.lsfn.Shared.Subscription.SubscriptionMessageParser;

public class AccelerateEastWestParser extends SubscriptionMessageParser {

	protected AccelerateEastWestParser() {
		super(new AccelerateEastWest(UnitDirection.NOWHERE));
	}

	@Override
	public Subscribeable parse_subscription_update(Subscription_update s) throws PublishFailedException {
		return new AccelerateEastWest(UnitDirection.lookup(0, s.getInt32Value()));
	}
}