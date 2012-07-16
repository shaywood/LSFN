package com.wikispaces.lsfn.Interface;

import com.wikispaces.lsfn.Shared.LSFN.SI.Subscription_output_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.SubscribeableOutput;

public class TestParser extends SubscriptionMessageParser {

	protected TestParser() {
		super(SubscribeableOutput.TEST);
	}

	@Override
	public void parse_subscription_update(Subscription_update s) throws SubscriptionMessageParser.PublishFailedException {
		if(!s.getStringValue().equals(SubscribeableOutput.TEST_MESSAGE)) {
			Publish_Failed_Exception(s.getStringValue());
		}
	}
}
