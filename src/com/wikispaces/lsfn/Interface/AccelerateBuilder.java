package com.wikispaces.lsfn.Interface;

import com.wikispaces.Shared.Messaging.Accelerate;
import com.wikispaces.Shared.Messaging.Message;
import com.wikispaces.Shared.Messaging.MessageBuilder;
import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;

public class AccelerateBuilder extends MessageBuilder {
	protected AccelerateBuilder() {
		super(new Accelerate(UnitDirection.NOWHERE));
	}

	public Subscription_update build_subscription_update(Message s) {
		Accelerate a = (Accelerate)s;
		
		return Subscription_update.newBuilder()
				.setID(a.get_id())
				.addInt32Value(a.get_direction().get_north_south())
				.addInt32Value(a.get_direction().get_east_west())
				.build();
	}
}