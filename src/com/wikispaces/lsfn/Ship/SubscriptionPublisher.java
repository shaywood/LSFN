package com.wikispaces.lsfn.Ship;

import java.util.HashSet;
import java.util.Set;


import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates;
import com.wikispaces.lsfn.Shared.LSFN.SI;
import com.wikispaces.lsfn.Shared.Subscription.NoSubscriptionBuilderDefinedException;
import com.wikispaces.lsfn.Shared.Subscription.Subscribeable;
import com.wikispaces.lsfn.Shared.Subscription.SubscriptionMessageBuilder;
import com.wikispaces.lsfn.Shared.Subscription.SubscriptionMessageBuilderFactory;

// Builds up a subscription outputs section to for subscribed INT_clients.
public class SubscriptionPublisher {
	private Subscriptions subscriber;
	private SubscriptionMessageBuilderFactory builders;
	
	public SubscriptionPublisher(Subscriptions subscriber, SubscriptionMessageBuilderFactory builders) {
		this.subscriber = subscriber;
		this.builders = builders;
	}
	
	public void add_subscription_outputs_data(SI.Builder message_builder, int INT_id, Iterable<Subscribeable> updates) throws UnknownInterfaceClientException, NoSubscriptionBuilderDefinedException {
		
		Set<Integer> subscriptions = subscriber.get_subscription_ids(INT_id);
		Set<Subscribeable> updated_subscriptions = updated_subscriptions(subscriptions, updates);
		
		if(updated_subscriptions.size() > 0) {
			Subscription_updates.Builder builder = Subscription_updates.newBuilder(); 
			
			for (Subscribeable s : updated_subscriptions) {
				SubscriptionMessageBuilder s_builder = builders.get_builder(s);
				builder.addUpdates(s_builder.build_subscription_update(s));
			}
			message_builder.setOutputUpdates(builder.build());
		}
	}

	private Set<Subscribeable> updated_subscriptions(Set<Integer> subscriptions, Iterable<Subscribeable> updates) {
		Set<Subscribeable> updated_subscriptions = new HashSet<Subscribeable>();
		
		for(Subscribeable u : updates) {
			if(subscriptions.contains(u.get_id())) {
				updated_subscriptions.add(u);
			}
		}
		return updated_subscriptions;
	}
}
