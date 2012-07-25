package com.wikispaces.Shared.Messaging;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.wikispaces.Shared.Messaging.MessageFactory.SubscribeableNotFoundException;
import com.wikispaces.lsfn.Shared.LSFN.IS;

// If anyone can think of a better name for it, please change this.
public class SubscriptionRequest {
	private Set<Integer> available_subscription_ids;
	private MessageFactory subscribeable_factory;

	public SubscriptionRequest(MessageFactory subscribeable_factory, Set<Message> available_subscriptions) {
		this.subscribeable_factory = subscribeable_factory;
		
		this.available_subscription_ids = new HashSet<Integer>();
		for(Message s : available_subscriptions) {
			available_subscription_ids.add(s.get_id());
		}
	}
	
	public IS build_message(List<Message> requested_subscriptions) throws UnavailableSubscriptionException {
		Set<Integer> ids = new HashSet<Integer>(); 
		Set<Message> refusals = new HashSet<Message>();
		for (Message request : requested_subscriptions) {
			if(!available_subscription_ids.contains(request.get_id())) {
				refusals.add(request);
			}
			ids.add(request.get_id());
		}
		
		if(refusals.size() > 0) {
			throw new UnavailableSubscriptionException(refusals);
		}
		
		return IS.newBuilder().setSubscribe(
				IS.Subscribe.newBuilder().addAllOutputIDs(ids))
			.build();
	}
	
	public Set<Message> parse_message(IS message) throws SubscribeableNotFoundException, UnavailableSubscriptionException {
		
    	List<Integer> ids = message.getSubscribe().getOutputIDsList();
    	Set<Message> acceptances = new HashSet<Message>();
    	Set<Message> refusals = new HashSet<Message>();
    	
    	for(Integer id : ids) {
    		Message request = subscribeable_factory.lookup_by_id(id);
    		if(available_subscription_ids.contains(request.get_id())) {
    			acceptances.add(request);
    		}
    		else {
    			refusals.add(request);
    		}
    	}
    	
    	if(refusals.size() > 0) {
    		throw new UnavailableSubscriptionException(refusals);
    	}
    	
    	return acceptances;
    }
}
