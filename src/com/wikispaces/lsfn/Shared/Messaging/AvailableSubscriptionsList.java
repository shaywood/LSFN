package com.wikispaces.lsfn.Shared.Messaging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.wikispaces.lsfn.Shared.LSFN.SI;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details;
import com.wikispaces.lsfn.Shared.Messaging.MessageFactory.SubscribeableNotFoundException;

public class AvailableSubscriptionsList {
	private MessageFactory subscribeable_factory;

	public AvailableSubscriptionsList(MessageFactory subscribeable_factory) {
		this.subscribeable_factory = subscribeable_factory;
	}
	
	
    public SI build_message(Integer INT_ID) {
    	List<Value_details> available_subscriptions = new ArrayList<Value_details>();
    	
    	for(Message s : subscribeable_factory.get_outputs()) {
    		available_subscriptions.add(build_value_details(s));
    	}
    	
    	return SI.newBuilder()
			.setSubscriptionsAvailable(
				Subscriptions_available.newBuilder().addAllOutputs(available_subscriptions))
			.build();
	}
    
    private Value_details build_value_details(Message subscribeable) {
    	return Value_details.newBuilder()
    			.setID(subscribeable.get_id())
    			.setName(subscribeable.get_description())
    			.setType(subscribeable.get_value_type())
    			.build();
    }
    
    public Set<Message> parse_message(SI message) throws SubscribeableNotFoundException {
    	List<Value_details> value_details = message.getSubscriptionsAvailable().getOutputsList();
    	Set<Message> available_subscriptions = new HashSet<Message>();
    	for (Value_details v : value_details) {
    		available_subscriptions.add(subscribeable_factory.lookup_by_id(v.getID()));
    	}
    	return available_subscriptions;
    }
}
