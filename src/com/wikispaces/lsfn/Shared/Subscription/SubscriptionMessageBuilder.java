package com.wikispaces.lsfn.Shared.Subscription;

import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;


public abstract class SubscriptionMessageBuilder {
	
	private final Subscribeable accepted_subscribeable;

	protected SubscriptionMessageBuilder(Subscribeable accepted_subscribeable) {
		this.accepted_subscribeable = accepted_subscribeable;
	}
	
	public abstract Subscription_update build_subscription_update(Subscribeable s);

	public boolean builds_subscribeable(Subscribeable s) {
		return s.get_id() == accepted_subscribeable.get_id();
	}
}
