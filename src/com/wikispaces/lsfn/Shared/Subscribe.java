package com.wikispaces.lsfn.Shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.Subscriptions.Subscribeable;

// If anyone can think of a better name for it, please change this.
public class Subscribe {
	private Set<Subscribeable> available_subscriptions;

	public Subscribe(Set<Subscribeable> available_subscriptions) {
		this.available_subscriptions = available_subscriptions;
	}
	
	public IS build_message(List<Subscribeable> requested_subscriptions) throws UnavailableSubscriptionExeption {
		List<Integer> ids = new ArrayList<Integer>(); 
		Set<Subscribeable> refusals = new HashSet<Subscribeable>();
		for (Subscribeable request : requested_subscriptions) {
			if(!available_subscriptions.contains(request)) {
				refusals.add(request);
			}
			ids.add(request.get_id());
		}
		
		if(refusals.size() > 0) {
			throw new UnavailableSubscriptionExeption(refusals);
		}
		
		return IS.newBuilder().setSubscribe(
				IS.Subscribe.newBuilder().addAllOutputIDs(ids))
			.build();
	}
	
	public Set<Subscribeable> parse_message(IS message) throws SubscribeableNotFoundException, UnavailableSubscriptionExeption {
		
    	List<Integer> ids = message.getSubscribe().getOutputIDsList();
    	Set<Subscribeable> acceptances = new HashSet<Subscribeable>();
    	Set<Subscribeable> refusals = new HashSet<Subscribeable>();
    	
    	for(Integer id : ids) {
    		Subscribeable request = Subscribeable.lookup_by_id(id);
    		if(available_subscriptions.contains(request)) {
    			acceptances.add(request);
    		}
    		else {
    			refusals.add(request);
    		}
    	}
    	
    	if(refusals.size() > 0) {
    		throw new UnavailableSubscriptionExeption(refusals);
    	}
    	
    	return acceptances;
    }
}
