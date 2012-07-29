package com.wikispaces.lsfn.Shared.Messaging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Messaging.MessageFactory.SubscribeableNotFoundException;
import com.wikispaces.lsfn.Shared.Messaging.MessageParser.PublishFailedException;

public class MessageParserFactory {
	private Set<MessageParser> parsers;
	private MessageFactory subscribeable_factory;

	public MessageParserFactory(MessageFactory subscribeable_factory, MessageParser... parsers) {
		this.subscribeable_factory = subscribeable_factory;
		this.parsers = new HashSet<MessageParser>(Arrays.asList(parsers));
	}
	
	public Set<Message> parse_subscription_data(Subscription_updates updates) throws PublishFailedException, NoMessageParserDefinedException, SubscribeableNotFoundException  {
		Set<Message> parsed_updates = new HashSet<Message>();
		for (Subscription_update u : updates.getUpdatesList()) {
			Message s = subscribeable_factory.lookup_by_id(u.getID());
			parsed_updates.add(get_parser(s).parse_subscription_update(u));
		}
		return parsed_updates;
	}
	
	private MessageParser get_parser(Message s) throws NoMessageParserDefinedException {
		for (MessageParser p : parsers) {
			if(p.parses_subscribeable(s)) {
				return p;
			}
		}
		throw new NoMessageParserDefinedException(s);
	}
}
