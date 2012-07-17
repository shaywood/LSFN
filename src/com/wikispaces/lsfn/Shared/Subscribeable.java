package com.wikispaces.lsfn.Shared;

import java.util.HashSet;
import java.util.Set;

import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details.Value_type;

public enum Subscribeable {
	TEST(1, "Test subscribeable output.", Value_type.STRING, SubscriptionType.OUTPUT),
	ACCELERATE_NORTHSOUTH(2, "North-south acceleration input.", Value_type.STRING, SubscriptionType.INPUT),
	ACCELERATE_EASTWEST(3, "East-west acceleration input.", Value_type.STRING, SubscriptionType.INPUT);
	
	public static final String TEST_MESSAGE = "This is a test message to ensure that subscription is working.";
	
	private int id;
	private String description;
	private Value_type value_type;
	private SubscriptionType type;
	
	private static Set<Subscribeable> outputs;
	
	public int get_id() {
		return id;
	}
	
	public String get_description() {
		return description;
	}
	
	public Value_type get_value_type() {
		return value_type;
	}
	
	private Subscribeable(int id, String description, Value_type value_type, SubscriptionType type) {
		this.id = id;
		this.description = description;
		this.value_type = value_type;
		this.type = type;
	}
	
	public static Set<Subscribeable> get_output_subscribeables() {
		if(outputs == null)
		{
			outputs = new HashSet<Subscribeable>();
			for(Subscribeable s : Subscribeable.values()) {
				if(s.type == SubscriptionType.OUTPUT) {
					outputs.add(s);
				}
			}
		}
		
		return outputs;
		/* In the future, we may want some way to filter some of these out (e.g. based on ship capabilities, or disabled features in the environment config). 
		 * When that happens, we should probably move this method out into another class. */
	}
	
	public static Subscribeable lookup_by_id(int id) throws SubscribeableNotFoundException {
		for (Subscribeable s : Subscribeable.values()) {
			if(id == s.id) {
				return s;
			}
		}
			
		throw new SubscribeableNotFoundException(id); 
	}
}
