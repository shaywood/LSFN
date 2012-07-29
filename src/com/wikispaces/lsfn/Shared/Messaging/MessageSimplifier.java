package com.wikispaces.lsfn.Shared.Messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MessageSimplifier {
	public List<Message> merge(List<Message> commands) {
		Map<Class<? extends Message>, List<Message>> groups = new HashMap<Class<? extends Message>, List<Message>>();
		
		for(Message c : commands) {
			Class<? extends Message> type = c.getClass();
			if(!groups.containsKey(type)){
				groups.put(type, new ArrayList<Message>());
			}
			groups.get(type).add(c);
		}
		
		List<Message> results = new ArrayList<Message>();
		for(List<Message> group : groups.values()) {
			results.add(merge_group(group));
		}
		return results;
	}

	private Message merge_group(List<Message> commands) {
		Message merged = null;
		for(Message c : commands)
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
