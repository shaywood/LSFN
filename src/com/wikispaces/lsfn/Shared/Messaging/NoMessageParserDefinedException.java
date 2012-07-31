package com.wikispaces.lsfn.Shared.Messaging;



@SuppressWarnings("serial")
public class NoMessageParserDefinedException extends Exception {
	public NoMessageParserDefinedException(Message s) {
		super("No subscription message parser defined for Subscribeable with id " + s.get_id() + " and name " + s.get_description());
	}
}
