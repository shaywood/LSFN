package com.wikispaces.lsfn.Shared.Messaging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MessageBuilderFactory {
	private Set<MessageBuilder> builders;

	public MessageBuilderFactory(MessageBuilder... builders) {
		this.builders = new HashSet<MessageBuilder>(Arrays.asList(builders));
	}
	
	public MessageBuilder get_builder(Message s) throws NoMessageBuilderDefinedException {
		for (MessageBuilder b : builders) {
			if(b.builds_subscribeable(s)) {
				return b;
			}
		}
		throw new NoMessageBuilderDefinedException(s);
	}
}
