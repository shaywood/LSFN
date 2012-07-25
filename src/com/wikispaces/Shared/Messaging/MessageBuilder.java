package com.wikispaces.Shared.Messaging;

import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;


public abstract class MessageBuilder {
	
	private final Message accepted_subscribeable;

	protected MessageBuilder(Message accepted_subscribeable) {
		this.accepted_subscribeable = accepted_subscribeable;
	}
	
	public abstract Subscription_update build_subscription_update(Message s);

	public boolean builds_subscribeable(Message s) {
		return s.get_id() == accepted_subscribeable.get_id();
	}
}
