package com.wikispaces.lsfn.Interface;


import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Subscription.Subscribeable;
import com.wikispaces.lsfn.Shared.Subscription.SubscriptionMessageParser;
import com.wikispaces.lsfn.Shared.Subscription.Test;

public class TestParser extends SubscriptionMessageParser {

	protected TestParser() {
		super(new Test());
	}

	@Override
	public Subscribeable parse_subscription_update(Subscription_update s) throws SubscriptionMessageParser.PublishFailedException {
		Test test = new Test();
		
		if(!s.getStringValue(0).equals(test.get_test_message())) {
			throw new PublishFailedException(accepted_subscribeable, s.getStringValue(0));
		}
		
		return test;
	}
}
