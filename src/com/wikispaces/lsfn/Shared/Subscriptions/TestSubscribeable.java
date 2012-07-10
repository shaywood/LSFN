package com.wikispaces.lsfn.Shared.Subscriptions;

import com.wikispaces.lsfn.Shared.LSFN.SI.Subscription_output_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details.Value_type;

public class TestSubscribeable extends Subscribeable {
	private static final String TEST_MESSAGE = "This is a test message to ensure that subscription is working.";
	
	public TestSubscribeable() {
		super(1, "Test subscribeable.", Value_type.STRING);
	}
	
	public Subscription_update build_subscription_update() {
		return Subscription_update.newBuilder()
			.setOutputID(get_id())
			.setStringValue(TEST_MESSAGE)
			.build();
	}
	
	public void parse_subscription_update(Subscription_update s) throws Subscribeable.PublishFailedException {
		if(s.getStringValue() != TEST_MESSAGE) {
			Publish_Failed_Exception(s.getStringValue());
		}
	}
}
