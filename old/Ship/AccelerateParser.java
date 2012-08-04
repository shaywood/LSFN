package com.wikispaces.lsfn.Ship;

import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Messaging.Accelerate;
import com.wikispaces.lsfn.Shared.Messaging.Message;
import com.wikispaces.lsfn.Shared.Messaging.MessageParser;

public class AccelerateParser extends MessageParser {

	protected AccelerateParser() {
		super(new Accelerate(UnitDirection.NOWHERE));
	}

	@Override
	public Message parse_subscription_update(Subscription_update s) throws PublishFailedException {
		return new Accelerate(UnitDirection.lookup(s.getInt32Value(0), s.getInt32Value(1)));
	}
}