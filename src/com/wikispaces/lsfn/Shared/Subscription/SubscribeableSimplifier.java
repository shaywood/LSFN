package com.wikispaces.lsfn.Shared.Subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SubscribeableSimplifier {
	public List<Subscribeable> merge(List<Subscribeable> commands) {
		Map<Class<? extends Subscribeable>, List<Subscribeable>> groups = new HashMap<Class<? extends Subscribeable>, List<Subscribeable>>();
		
		for(Subscribeable c : commands) {
			Class<? extends Subscribeable> type = c.getClass();
			if(!groups.containsKey(type)){
				groups.put(type, new ArrayList<Subscribeable>());
			}
			groups.get(type).add(c);
		}
		
		List<Subscribeable> results = new ArrayList<Subscribeable>();
		for(List<Subscribeable> group : groups.values()) {
			results.add(merge_group(group));
		}
		return results;
	}

	private Subscribeable merge_group(List<Subscribeable> commands) {
		Subscribeable merged = null;
		for(Subscribeable c : commands)
		{
			if(merged == null) {
				merged = c;
			}
			else {
				merged = merged.combine_with(c);
			}
		}
		return merged;
	}
}
