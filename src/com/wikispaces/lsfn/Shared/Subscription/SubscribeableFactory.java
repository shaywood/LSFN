package com.wikispaces.lsfn.Shared.Subscription;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.wikispaces.lsfn.Shared.UnitDirection;

public class SubscribeableFactory {
	private Set<Subscribeable> outputs = new HashSet<Subscribeable>(Arrays.asList(
		(Subscribeable)new Test()
	));
	private Set<Subscribeable> inputs = new HashSet<Subscribeable>(Arrays.asList(
		(Subscribeable)new AccelerateNorthSouth(UnitDirection.NOWHERE),
		(Subscribeable)new AccelerateEastWest(UnitDirection.NOWHERE)
	));
	
	public Subscribeable lookup_by_id(int id) throws SubscribeableNotFoundException {
		for (Subscribeable s : outputs) {
			if(id == s.get_id()) {
				return s;
			}
		}
		
		for (Subscribeable s : inputs) {
			if(id == s.get_id()) {
				return s;
			}
		}
			
		throw new SubscribeableNotFoundException(id); 
	}
	
	public Set<Subscribeable> get_outputs() {
		return outputs;
	}
	
	public Set<Subscribeable> get_inputs() {
		return outputs;
	}
	
	@SuppressWarnings("serial")
	public class SubscribeableNotFoundException extends Exception {
		public SubscribeableNotFoundException(int id) {
			super("No subscribeable service exists with id: " + id);
		}
	}
}
