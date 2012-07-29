package com.wikispaces.lsfn.Shared.Messaging;


@SuppressWarnings("serial")
public class NoMessageBuilderDefinedException extends Exception {
	public NoMessageBuilderDefinedException(Message s) {
		super("No subscription message builder defined for Subscribeable with id " + s.get_id() + " and name " + s.get_description());
	}
}
