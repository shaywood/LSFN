package com.wikispaces.Shared.Messaging;

import java.util.Set;



@SuppressWarnings("serial")
public class UnavailableSubscriptionException extends Exception {
	public UnavailableSubscriptionException(Set<Message> refusals) {
		super("Interface wanted the following subscriptions, which were not provided by the Ship server:\\r\\n" + build_refusal_list(refusals));
	}
	
	private static String build_refusal_list(Set<Message> refusals) {
		StringBuilder sb = new StringBuilder();
	    for(Message r: refusals) {
	    	sb.append("id: " + r.get_id() + " name: " + r.get_description() + "\\r\\n");
	    }
	    return sb.toString();
	}
}
