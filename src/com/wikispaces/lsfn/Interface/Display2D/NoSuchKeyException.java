package com.wikispaces.lsfn.Interface.Display2D;

@SuppressWarnings("serial")
public class NoSuchKeyException extends Exception {
	public NoSuchKeyException(String key) {
		super("Attempted to bind or unbind a key which does not exist: " + key);
	}
}
