package com.wikispaces.lsfn.Shared.Subscription;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.Subscription.SubscribeableFactory.SubscribeableNotFoundException;

// If anyone can think of a better name for it, please change this.
public class SubscriptionRequest {
	private Set<Integer> available_subscription_ids;
	private SubscribeableFactory subscribeable_factory;

	public SubscriptionRequest(SubscribeableFactory subscribeable_factory, Set<Subscribeable> available_subscriptions) {
		this.subscribeable_factory = subscribeable_factory;
		
		this.available_subscription_ids = new HashSet<Integer>();
		for(Subscribeable s : available_subscriptions) {
			available_subscription_ids.add(s.get_id());
		}
	}
	
	public IS build_message(List<Subscribeable> requested_subscriptions) throws UnavailableSubscriptionException {
		Set<Integer> ids = new HashSet<Integer>(); 
		Set<Subscribeable> refusals = new HashSet<Subscribeable>();
		for (Subscribeable request : requested_subscriptions) {
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
	
	public Set<Subscribeable> parse_message(IS message) throws SubscribeableNotFoundException, UnavailableSubscriptionException {
		
    	List<Integer> ids = message.getSubscribe().getOutputIDsList();
    	Set<Subscribeable> acceptances = new HashSet<Subscribeable>();
    	Set<Subscribeable> refusals = new HashSet<Subscribeable>();
    	
    	for(Integer id : ids) {
    		Subscribeable request = subscribeable_factory.lookup_by_id(id);
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
