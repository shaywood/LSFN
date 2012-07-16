package com.wikispaces.lsfn.Shared;

import java.util.Set;


@SuppressWarnings("serial")
public class UnavailableSubscriptionException extends Exception {
	public UnavailableSubscriptionException(Set<SubscribeableOutput> refusals) {
		super("Interface wanted the following subscriptions, which were not provided by the Ship server:\\r\\n" + build_refusal_list(refusals));
	}
	
	private static String build_refusal_list(Set<SubscribeableOutput> refusals) {
		StringBuilder sb = new StringBuilder();
	    for(SubscribeableOutput r: refusals) {
	    	sb.append("id: " + r.get_id() + " name: " + r.get_description() + "\\r\\n");
	    }
	    return sb.toString();
	}
}
