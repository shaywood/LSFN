package com.wikispaces.lsfn.Shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.wikispaces.lsfn.Shared.LSFN.SI;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details;

public class ListAvailableSubscriptions {

    public SI build_message(Integer INT_ID) {
    	List<Value_details> available_subscriptions = new ArrayList<Value_details>();
    	
    	for(SubscribeableOutput s : SubscribeableOutput.get_all_available_subscribeables()) {
    		available_subscriptions.add(build_value_details(s));
    	}
    	
    	return SI.newBuilder()
			.setSubscriptionsAvailable(
				Subscriptions_available.newBuilder().addAllOutputs(available_subscriptions))
			.build();
	}
    
    private Value_details build_value_details(SubscribeableOutput subscribeable) {
    	return Value_details.newBuilder()
    			.setID(subscribeable.get_id())
    			.setName(subscribeable.get_description())
    			.setType(subscribeable.get_value_type())
    			.build();
    }
    
    public Set<SubscribeableOutput> parse_message(SI message) throws SubscribeableNotFoundException {
    	List<Value_details> value_details = message.getSubscriptionsAvailable().getOutputsList();
    	Set<SubscribeableOutput> available_subscriptions = new HashSet<SubscribeableOutput>();
    	for (Value_details v : value_details) {
    		available_subscriptions.add(SubscribeableOutput.lookup_by_id(v.getID()));
    	}
    	return available_subscriptions;
    }
}
