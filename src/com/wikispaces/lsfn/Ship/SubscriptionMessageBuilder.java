package com.wikispaces.lsfn.Ship;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.wikispaces.lsfn.Shared.LSFN.SI.Subscription_output_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.SubscribeableOutput;

public abstract class SubscriptionMessageBuilder {
	private static Set<SubscriptionMessageBuilder> builders = new HashSet<SubscriptionMessageBuilder>(Arrays.asList(
		new TestBuilder()
	));
	
	protected SubscribeableOutput subscribeable;
	
	protected SubscriptionMessageBuilder(SubscribeableOutput subscribeable) {
		this.subscribeable = subscribeable;
	}
	
	public abstract Subscription_update build_subscription_update();
	public abstract boolean has_updated();

	public static SubscriptionMessageBuilder get_builder(SubscribeableOutput s) throws NoSubscriptionBuilderDefinedException {
		for (SubscriptionMessageBuilder b : builders) {
			if(b.subscribeable == s) {
				return b;
			}
		}
		throw new NoSubscriptionBuilderDefinedException(s);
	}
}