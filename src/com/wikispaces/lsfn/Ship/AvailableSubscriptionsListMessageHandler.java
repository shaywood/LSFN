package com.wikispaces.lsfn.Ship;

import java.util.ArrayList;
import java.util.List;

import com.wikispaces.lsfn.Shared.ClientHandler;
import com.wikispaces.lsfn.Shared.LSFN.SI;

public class AvailableSubscriptionsListMessageHandler {
	private final ClientHandler INT_server;

	public AvailableSubscriptionsListMessageHandler(ClientHandler INT_server) {
		this.INT_server = INT_server;
	}
	
    public void send_available_subscriptions_list(Integer INT_ID) {
    	List<SI.Subscriptions_available.Value_details> available_subscriptions = new ArrayList<SI.Subscriptions_available.Value_details>();
    	
    	for(Subscribeable s : Subscribeable.get_all_available_subscribeables()) {
    		available_subscriptions.add(build_value_details(s));
    	}
    	
    	SI subscriptions_list = SI.newBuilder()
			.setSubscriptionsAvailable(
				SI.Subscriptions_available.newBuilder().addAllOutputs(available_subscriptions))
			.build();

	    INT_server.send(INT_ID, subscriptions_list.toByteArray());
	}
    
    private SI.Subscriptions_available.Value_details build_value_details(Subscribeable subscribeable) {
    	return SI.Subscriptions_available.Value_details.newBuilder()
    			.setID(subscribeable.get_id())
    			.setName(subscribeable.get_description())
    			.setType(subscribeable.get_value_type())
    			.build();
    }
}
