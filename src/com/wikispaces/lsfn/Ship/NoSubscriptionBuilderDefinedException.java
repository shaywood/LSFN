package com.wikispaces.lsfn.Ship;

import com.wikispaces.lsfn.Shared.Subscribeable;

@SuppressWarnings("serial")
public class NoSubscriptionBuilderDefinedException extends Exception {
	public NoSubscriptionBuilderDefinedException(Subscribeable s) {
		super("No subscription message builder defined for Subscribeable with id " + s.get_id() + " and name " + s.get_description());
	}
}