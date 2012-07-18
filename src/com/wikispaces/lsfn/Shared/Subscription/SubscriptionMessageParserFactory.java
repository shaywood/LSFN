package com.wikispaces.lsfn.Shared.Subscription;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Subscription.SubscribeableFactory.SubscribeableNotFoundException;
import com.wikispaces.lsfn.Shared.Subscription.SubscriptionMessageParser.PublishFailedException;

public class SubscriptionMessageParserFactory {
	private Set<SubscriptionMessageParser> parsers;
	private SubscribeableFactory subscribeable_factory;

	public SubscriptionMessageParserFactory(SubscribeableFactory subscribeable_factory, SubscriptionMessageParser... parsers) {
		this.subscribeable_factory = subscribeable_factory;
		this.parsers = new HashSet<SubscriptionMessageParser>(Arrays.asList(parsers));
	}
	
	public Set<Subscribeable> parse_subscription_data(Subscription_updates updates) throws PublishFailedException, NoSubscriptionParserDefinedException, SubscribeableNotFoundException  {
		Set<Subscribeable> parsed_updates = new HashSet<Subscribeable>();
		for (Subscription_update u : updates.getUpdatesList()) {
			Subscribeable s = subscribeable_factory.lookup_by_id(u.getID());
			parsed_updates.add(get_parser(s).parse_subscription_update(u));
		}
		return parsed_updates;
	}
	
	private SubscriptionMessageParser get_parser(Subscribeable s) throws NoSubscriptionParserDefinedException {
		for (SubscriptionMessageParser p : parsers) {
			if(p.parses_subscribeable(s)) {
				return p;
			}
		}
		throw new NoSubscriptionParserDefinedException(s);
	}
}
