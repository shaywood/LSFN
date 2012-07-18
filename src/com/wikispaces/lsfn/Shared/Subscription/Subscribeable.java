package com.wikispaces.lsfn.Shared.Subscription;

import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details.Value_type;

public abstract class Subscribeable {	
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
	
	protected Subscribeable(int id, String description, Value_type value_type) {
		this.id = id;
		this.description = description;
		this.value_type = value_type;
	}
	
	public abstract boolean can_combine(Subscribeable c);
	public abstract Subscribeable combine_with(Subscribeable c);
}
