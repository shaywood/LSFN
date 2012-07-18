package com.wikispaces.lsfn.Shared.Subscription;

import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details.Value_type;

public class Test extends Subscribeable {
	public String get_test_message() {
		return "This is a test message to ensure that subscription is working.";
	}
	
	public Test() {
		super(1, "Test subscribeable output.", Value_type.STRING);
	}
	
	@Override
	public boolean can_combine(Subscribeable c) {
		return false;
	}

	@Override
	public Subscribeable combine_with(Subscribeable c) {
		return this; 
	}
}
