package com.wikispaces.lsfn.Shared;

@SuppressWarnings("serial")
public class SubscribeableNotFoundException extends Exception {
	public SubscribeableNotFoundException(int id) {
		super("No subscribeable service exists with id: " + id);
	}
}
