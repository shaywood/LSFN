package com.wikispaces.lsfn.Shared;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details.Value_type;

public enum SubscribeableOutput {
	TEST(1, "Test subscribeable output.", Value_type.STRING);
	
	public static final String TEST_MESSAGE = "This is a test message to ensure that subscription is working.";
	
	private int id;
	private String description;
	private Value_type value_type;
	
	public int get_id() {
		return id;
	}
	
	public String get_description() {
		return description;
	}
	
	public Value_type get_value_type() {
		return value_type;
	}
	
	private SubscribeableOutput(int id, String description, Value_type value_type) {
		this.id = id;
		this.description = description;
		this.value_type = value_type;
	}
	
	public static Set<SubscribeableOutput> get_all_available_subscribeables() {
		return new HashSet<SubscribeableOutput>(Arrays.asList(SubscribeableOutput.values())); 
		/* In the future, we may want some way to filter some of these out (e.g. based on ship capabilities, or disabled features in the environment config). 
		 * When that happens, we should probably move this method out into another class. */
	}
	
	public static SubscribeableOutput lookup_by_id(int id) throws SubscribeableNotFoundException {
		for (SubscribeableOutput s : SubscribeableOutput.values()) {
			if(id == s.id) {
				return s;
			}
		}
			
		throw new SubscribeableNotFoundException(id); 
	}
}
