package com.wikispaces.lsfn.Ship;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Subscribeable;

public abstract class SubscriptionMessageBuilder {
	private static Set<SubscriptionMessageBuilder> builders = new HashSet<SubscriptionMessageBuilder>(Arrays.asList(
		new TestBuilder()
	));
	
	protected Subscribeable subscribeable;
	
	protected SubscriptionMessageBuilder(Subscribeable subscribeable) {
		this.subscribeable = subscribeable;
	}
	
	public abstract Subscription_update build_subscription_update();
	public abstract boolean has_updated();

	public static SubscriptionMessageBuilder get_builder(Subscribeable s) throws NoSubscriptionBuilderDefinedException {
		for (SubscriptionMessageBuilder b : builders) {
			if(b.subscribeable == s) {
				return b;
			}
		}
		throw new NoSubscriptionBuilderDefinedException(s);
	}
}