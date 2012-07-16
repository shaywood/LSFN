package com.wikispaces.lsfn.Interface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.wikispaces.lsfn.Shared.LSFN.SI.Subscription_output_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.SubscribeableOutput;

public abstract class SubscriptionMessageParser {
	private static Set<SubscriptionMessageParser> parsers = new HashSet<SubscriptionMessageParser>(Arrays.asList(
			new TestParser()
	));
	
	protected SubscribeableOutput subscribeable;
	
	protected SubscriptionMessageParser(SubscribeableOutput subscribeable) {
		this.subscribeable = subscribeable;
	}
	
	protected void Publish_Failed_Exception(String failed_value) throws PublishFailedException {
		throw new PublishFailedException(this.subscribeable, failed_value);
	}

	public abstract void parse_subscription_update(Subscription_update s) throws PublishFailedException;

	@SuppressWarnings("serial")
	public class PublishFailedException extends Exception {
		public PublishFailedException(SubscribeableOutput s, String failed_value) {
			super("Publishing subcription with id " + s.get_id() + " and name " + s.get_description() + " failed. Received value: " + failed_value);
		}
	}
	
	public static SubscriptionMessageParser get_parser(SubscribeableOutput s) throws NoSubscriptionParserDefinedException {
		for (SubscriptionMessageParser p : parsers) {
			if(p.subscribeable == s) {
				return p;
			}
		}
		throw new NoSubscriptionParserDefinedException(s);
	}
}
