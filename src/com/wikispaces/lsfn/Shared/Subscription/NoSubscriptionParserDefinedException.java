package com.wikispaces.lsfn.Shared.Subscription;



@SuppressWarnings("serial")
public class NoSubscriptionParserDefinedException extends Exception {
	public NoSubscriptionParserDefinedException(Subscribeable s) {
		super("No subscription message parser defined for Subscribeable with id " + s.get_id() + " and name " + s.get_description());
	}
}
