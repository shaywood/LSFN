package com.wikispaces.lsfn.Ship;

import com.wikispaces.Shared.Messaging.Accelerate;
import com.wikispaces.Shared.Messaging.Message;
import com.wikispaces.Shared.Messaging.MessageParser;
import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;

public class AccelerateParser extends MessageParser {

	protected AccelerateParser() {
		super(new Accelerate(UnitDirection.NOWHERE));
	}

	@Override
	public Message parse_subscription_update(Subscription_update s) throws PublishFailedException {
		return new Accelerate(UnitDirection.lookup(s.getInt32Value(0), s.getInt32Value(1)));
	}
}