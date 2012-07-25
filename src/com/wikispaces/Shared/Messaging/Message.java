package com.wikispaces.Shared.Messaging;

import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details.Value_type;

public abstract class Message {	
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
	
	protected Message(int id, String description, Value_type value_type) {
		this.id = id;
		this.description = description;
		this.value_type = value_type;
	}
	
	public abstract boolean can_combine(Message c);
	public abstract Message combine_with(Message c);
}
