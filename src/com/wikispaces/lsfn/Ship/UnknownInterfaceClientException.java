package com.wikispaces.lsfn.Ship;

@SuppressWarnings("serial")
public class UnknownInterfaceClientException extends Exception {
 
	public UnknownInterfaceClientException(int interface_client_id) {
		super("Unknown interface client: " + interface_client_id);
	}
}
