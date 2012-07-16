package com.wikispaces.lsfn.Ship;

import com.wikispaces.lsfn.Shared.SubscribeableOutput;

@SuppressWarnings("serial")
public class NoSubscriptionBuilderDefinedException extends Exception {
	public NoSubscriptionBuilderDefinedException(SubscribeableOutput s) {
		super("No subscription message builder defined for Subscribeable with id " + s.get_id() + " and name " + s.get_description());
	}
}