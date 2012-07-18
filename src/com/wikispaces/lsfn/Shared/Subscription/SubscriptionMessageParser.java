package com.wikispaces.lsfn.Shared.Subscription;

import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;

public abstract class SubscriptionMessageParser {
	
	protected final Subscribeable accepted_subscribeable;

	protected SubscriptionMessageParser(Subscribeable accepted_subscribeable) {
		this.accepted_subscribeable = accepted_subscribeable;
	}
	
	public abstract Subscribeable parse_subscription_update(Subscription_update s) throws PublishFailedException;

	@SuppressWarnings("serial")
	public class PublishFailedException extends Exception {
		public PublishFailedException(Subscribeable s, String failed_value) {
			super("Publishing subscriptios " + s + " failed. Received value: " + failed_value);
		}
	}

	public boolean parses_subscribeable(Subscribeable s) {
		return s.get_id() == accepted_subscribeable.get_id();
	}
}
