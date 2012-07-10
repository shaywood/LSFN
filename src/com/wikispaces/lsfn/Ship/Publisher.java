package com.wikispaces.lsfn.Ship;

import java.util.Set;

import com.wikispaces.lsfn.Shared.LSFN.SI;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscription_output_updates;
import com.wikispaces.lsfn.Shared.Subscriptions.Subscribeable;

// Builds up a subscription outputs section to for subscribed INT_clients.
// ToDo: a mechanism to tell us which parts of the model have changed, so we're not sending out updates for everything all the time.
public class Publisher {
	private Subscriptions subscriber;
	
	public Publisher(Subscriptions subscriber) {
		this.subscriber = subscriber;
	}
	
	public void add_subscription_outputs_data(SI.Builder message_builder, int INT_id) throws UnknownInterfaceClientException {
		
		Set<Subscribeable> subscriptions = subscriber.get_subscriptions(INT_id);
		boolean update_happened = false;
		Subscription_output_updates.Builder builder = Subscription_output_updates.newBuilder(); 
		
		for (Subscribeable s : subscriptions) {
			if(s.has_updated()) {
				builder.addUpdates(s.build_subscription_update());
				update_happened = true;
			}
		}
		
		if(update_happened) {
			message_builder.setOutputUpdates(builder.build());
		}
	}
}
