package com.wikispaces.lsfn.Interface;


import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Messaging.Message;
import com.wikispaces.lsfn.Shared.Messaging.MessageParser;
import com.wikispaces.lsfn.Shared.Messaging.Test;

public class TestParser extends MessageParser {

	protected TestParser() {
		super(new Test());
	}

	@Override
	public Message parse_subscription_update(Subscription_update s) throws MessageParser.PublishFailedException {
		Test test = new Test();
		
		if(!s.getStringValue(0).equals(test.get_test_message())) {
			throw new PublishFailedException(accepted_subscribeable, s.getStringValue(0));
		}
		
		return test;
	}
}
