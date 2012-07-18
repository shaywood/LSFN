package com.wikispaces.lsfn.Shared.Subscription;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SubscriptionMessageBuilderFactory {
	private Set<SubscriptionMessageBuilder> builders;

	public SubscriptionMessageBuilderFactory(SubscriptionMessageBuilder... builders) {
		this.builders = new HashSet<SubscriptionMessageBuilder>(Arrays.asList(builders));
	}
	
	public SubscriptionMessageBuilder get_builder(Subscribeable s) throws NoSubscriptionBuilderDefinedException {
		for (SubscriptionMessageBuilder b : builders) {
			if(b.builds_subscribeable(s)) {
				return b;
			}
		}
		throw new NoSubscriptionBuilderDefinedException(s);
	}
}
