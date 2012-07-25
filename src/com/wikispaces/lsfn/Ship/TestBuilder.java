package com.wikispaces.lsfn.Ship;


import com.wikispaces.Shared.Messaging.Message;
import com.wikispaces.Shared.Messaging.MessageBuilder;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;

public class TestBuilder extends MessageBuilder {
	public TestBuilder() {
		super(new com.wikispaces.Shared.Messaging.Test());
	}
	
	@Override
	public Subscription_update build_subscription_update(Message s) {
		com.wikispaces.Shared.Messaging.Test t = (com.wikispaces.Shared.Messaging.Test)s;
		return Subscription_update.newBuilder()
				.setID(t.get_id())
				.setStringValue(0, t.get_test_message())
				.build();
	}
}
