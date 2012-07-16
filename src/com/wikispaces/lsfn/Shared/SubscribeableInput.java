package com.wikispaces.lsfn.Shared;

import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details.Value_type;

public enum SubscribeableInput {
	TEST(1, "Test subscribeable input.", Value_type.STRING),
	ACCELERATE_NORTHSOUTH(2, "North-south acceleration input.", Value_type.STRING),
	ACCELERATE_EASTWEST(3, "East-west acceleration input.", Value_type.STRING);
	
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
	
	private SubscribeableInput(int id, String description, Value_type value_type) {
		this.id = id;
		this.description = description;
		this.value_type = value_type;
	}
	
	public static SubscribeableInput lookup_by_id(int id) throws SubscribeableNotFoundException {
		for (SubscribeableInput s : SubscribeableInput.values()) {
			if(id == s.id) {
				return s;
			}
		}
			
		throw new SubscribeableNotFoundException(id); 
	}
}
