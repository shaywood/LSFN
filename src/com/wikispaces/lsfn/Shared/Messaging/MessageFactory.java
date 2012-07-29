package com.wikispaces.lsfn.Shared.Messaging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.wikispaces.lsfn.Shared.UnitDirection;

public class MessageFactory {
	private Set<Message> outputs = new HashSet<Message>(Arrays.asList(
		(Message)new Test()
	));
	private Set<Message> inputs = new HashSet<Message>(Arrays.asList(
		(Message)new Accelerate(UnitDirection.NOWHERE)
	));
	
	public Message lookup_by_id(int id) throws SubscribeableNotFoundException {
		for (Message s : outputs) {
			if(id == s.get_id()) {
				return s;
			}
		}
		
		for (Message s : inputs) {
			if(id == s.get_id()) {
				return s;
			}
		}
			
		throw new SubscribeableNotFoundException(id); 
	}
	
	public Set<Message> get_outputs() {
		return outputs;
	}
	
	public Set<Message> get_inputs() {
		return outputs;
	}
	
	@SuppressWarnings("serial")
	public class SubscribeableNotFoundException extends Exception {
		public SubscribeableNotFoundException(int id) {
			super("No subscribeable service exists with id: " + id);
		}
	}
}
