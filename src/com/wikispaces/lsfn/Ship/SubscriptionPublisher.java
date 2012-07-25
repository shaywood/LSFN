package com.wikispaces.lsfn.Ship;

import java.util.HashSet;
import java.util.Set;


import com.wikispaces.Shared.Messaging.NoMessageBuilderDefinedException;
import com.wikispaces.Shared.Messaging.Message;
import com.wikispaces.Shared.Messaging.MessageBuilder;
import com.wikispaces.Shared.Messaging.MessageBuilderFactory;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates;
import com.wikispaces.lsfn.Shared.LSFN.SI;

// Builds up a subscription outputs section to for subscribed INT_clients.
public class SubscriptionPublisher {
	private Subscriptions subscriber;
	private MessageBuilderFactory builders;
	
	public SubscriptionPublisher(Subscriptions subscriber, MessageBuilderFactory builders) {
		this.subscriber = subscriber;
		this.builders = builders;
	}
	
	public void add_subscription_outputs_data(SI.Builder message_builder, int INT_id, Iterable<Message> updates) throws UnknownInterfaceClientException, NoMessageBuilderDefinedException {
		
		Set<Integer> subscriptions = subscriber.get_subscription_ids(INT_id);
		Set<Message> updated_subscriptions = updated_subscriptions(subscriptions, updates);
		
		if(updated_subscriptions.size() > 0) {
			Subscription_updates.Builder builder = Subscription_updates.newBuilder(); 
			
			for (Message s : updated_subscriptions) {
				MessageBuilder s_builder = builders.get_builder(s);
				builder.addUpdates(s_builder.build_subscription_update(s));
			}
			message_builder.setOutputUpdates(builder.build());
		}
	}

	private Set<Message> updated_subscriptions(Set<Integer> subscriptions, Iterable<Message> updates) {
		Set<Message> updated_subscriptions = new HashSet<Message>();
		
		for(Message u : updates) {
			if(subscriptions.contains(u.get_id())) {
				updated_subscriptions.add(u);
			}
		}
		return updated_subscriptions;
	}
}
