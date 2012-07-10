package com.wikispaces.lsfn.Interface;

import com.wikispaces.lsfn.Shared.Subscribeable;

@SuppressWarnings("serial")
public class NoSubscriptionParserDefinedException extends Exception {
	public NoSubscriptionParserDefinedException(Subscribeable s) {
		super("No subscription message parser defined for Subscribeable with id " + s.get_id() + " and name " + s.get_description());
	}
}
