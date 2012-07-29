package com.wikispaces.lsfn.Ship;

import java.util.*;

import com.wikispaces.lsfn.Shared.Messaging.Message;

public class Subscriptions {
	Map<Integer, Set<Message>> current = new HashMap<Integer, Set<Message>>();
	
	public Set<Integer> get_subscribers() {
		return current.keySet();
	}
	
	public void subscribe(int subscriber_id, Set<Message> subscribe_to) {
		if(!current.containsKey(subscriber_id)) {
			current.put(subscriber_id, new HashSet<Message>()); 
		}
		current.get(subscriber_id).addAll(subscribe_to);
	}
	
	public void unsubscribe(int subscriber_id, List<Message> unsubscribe_from) throws UnknownInterfaceClientException {
		if(current.containsKey(subscriber_id)) {
			throw new UnknownInterfaceClientException(subscriber_id); 
		}
		current.get(subscriber_id).removeAll(unsubscribe_from);
	}
	
	public Set<Message> get_subscriptions(int subscriber_id) throws UnknownInterfaceClientException {
		if(!current.containsKey(subscriber_id)) {
			throw new UnknownInterfaceClientException(subscriber_id);
		}
		return current.get(subscriber_id);
	}

	public Set<Integer> get_subscription_ids(int subscriber_id) throws UnknownInterfaceClientException {
		Set<Integer> ids = new HashSet<Integer>();
		for(Message s : get_subscriptions(subscriber_id)) {
			ids.add(s.get_id());
		}
		return ids;
	}
}
