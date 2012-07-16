package com.wikispaces.lsfn.Ship;

import java.util.*;

import com.wikispaces.lsfn.Shared.SubscribeableOutput;


public class Subscriptions {
	Map<Integer, Set<SubscribeableOutput>> current = new HashMap<Integer, Set<SubscribeableOutput>>();
	
	public Set<Integer> get_subscribers() {
		return current.keySet();
	}
	
	public void subscribe(int subscriber_id, Set<SubscribeableOutput> subscribe_to) {
		if(!current.containsKey(subscriber_id)) {
			current.put(subscriber_id, new HashSet<SubscribeableOutput>()); 
		}
		current.get(subscriber_id).addAll(subscribe_to);
	}
	
	public void unsubscribe(int subscriber_id, List<SubscribeableOutput> unsubscribe_from) throws UnknownInterfaceClientException {
		if(current.containsKey(subscriber_id)) {
			throw new UnknownInterfaceClientException(subscriber_id); 
		}
		current.get(subscriber_id).removeAll(unsubscribe_from);
	}
	
	public Set<SubscribeableOutput> get_subscriptions(int subscriber_id) throws UnknownInterfaceClientException {
		if(!current.containsKey(subscriber_id)) {
			throw new UnknownInterfaceClientException(subscriber_id);
		}
		return current.get(subscriber_id);
	}
}
