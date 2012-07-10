package com.wikispaces.lsfn.Shared;

import java.util.Set;


@SuppressWarnings("serial")
public class UnavailableSubscriptionExeption extends Exception {
	public UnavailableSubscriptionExeption(Set<Subscribeable> refusals) {
		super("Interface wanted the following subscriptions, which were not provided by the Ship server:\\r\\n" + build_refusal_list(refusals));
	}
	
	private static String build_refusal_list(Set<Subscribeable> refusals) {
		StringBuilder sb = new StringBuilder();
	    for(Subscribeable r: refusals) {
	    	sb.append("id: " + r.get_id() + " name: " + r.get_description() + "\\r\\n");
	    }
	    return sb.toString();
	}
}
