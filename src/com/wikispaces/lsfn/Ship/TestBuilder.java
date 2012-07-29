package com.wikispaces.lsfn.Ship;


import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Messaging.Message;
import com.wikispaces.lsfn.Shared.Messaging.MessageBuilder;

public class TestBuilder extends MessageBuilder {
	public TestBuilder() {
		super(new com.wikispaces.lsfn.Shared.Messaging.Test());
	}
	
	@Override
	public Subscription_update build_subscription_update(Message s) {
		com.wikispaces.lsfn.Shared.Messaging.Test t = (com.wikispaces.lsfn.Shared.Messaging.Test)s;
		return Subscription_update.newBuilder()
				.setID(t.get_id())
				.setStringValue(0, t.get_test_message())
				.build();
	}
}
