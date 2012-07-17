package com.wikispaces.lsfn.Ship;

import java.util.Set;

import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates;
import com.wikispaces.lsfn.Shared.Subscribeable;
import com.wikispaces.lsfn.Shared.LSFN.SI;

// Builds up a subscription outputs section to for subscribed INT_clients.
public class SubscriptionPublisher {
	private Subscriptions subscriber;
	
	public SubscriptionPublisher(Subscriptions subscriber) {
		this.subscriber = subscriber;
	}
	
	public void add_subscription_outputs_data(SI.Builder message_builder, int INT_id) throws UnknownInterfaceClientException, NoSubscriptionBuilderDefinedException {
		
		Set<Subscribeable> subscriptions = subscriber.get_subscriptions(INT_id);
		boolean update_happened = false;
		Subscription_updates.Builder builder = Subscription_updates.newBuilder(); 
		
		for (Subscribeable s : subscriptions) {
			SubscriptionMessageBuilder s_builder = SubscriptionMessageBuilder.get_builder(s);
			if(s_builder.has_updated()) {
				builder.addUpdates(s_builder.build_subscription_update());
				update_happened = true;
			}
		}
		
		if(update_happened) {
			message_builder.setOutputUpdates(builder.build());
		}
	}
}
