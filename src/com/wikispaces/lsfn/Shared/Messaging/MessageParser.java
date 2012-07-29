package com.wikispaces.lsfn.Shared.Messaging;

import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;

public abstract class MessageParser {
	
	protected final Message accepted_subscribeable;

	protected MessageParser(Message accepted_subscribeable) {
		this.accepted_subscribeable = accepted_subscribeable;
	}
	
	public abstract Message parse_subscription_update(Subscription_update s) throws PublishFailedException;

	@SuppressWarnings("serial")
	public class PublishFailedException extends Exception {
		public PublishFailedException(Message s, String failed_value) {
			super("Publishing subscriptios " + s + " failed. Received value: " + failed_value);
		}
	}

	public boolean parses_subscribeable(Message s) {
		return s.get_id() == accepted_subscribeable.get_id();
	}
}
