package com.wikispaces.lsfn.Ship;

import java.util.Arrays;
import java.util.List;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details.Value_type;

public enum Subscribeable {
	TEST(1, "Test subscribeable.", Value_type.STRING);

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

	private Subscribeable(int id, String description, Value_type value_type) {
		this.id = id;
		this.description = description;
		this.value_type = value_type;
	}
	
	public static List<Subscribeable> get_all_available_subscribeables() {
		return Arrays.asList(Subscribeable.values()); 
		/* In the future, we may want some way to filter some of these out (e.g. based on ship capabilities, or disabled features in the environment config). 
		 * When that happens, we should probably move this method out into another class. */
	}
}
