package com.wikispaces.lsfn.Interface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerCommandSimplifier {
	public List<PlayerCommand> merge(List<PlayerCommand> commands) {
		Map<Class<? extends PlayerCommand>, List<PlayerCommand>> groups = new HashMap<Class<? extends PlayerCommand>, List<PlayerCommand>>();
		
		for(PlayerCommand c : commands) {
			Class<? extends PlayerCommand> type = c.getClass();
			if(!groups.containsKey(type)){
				groups.put(type, new ArrayList<PlayerCommand>());
			}
			groups.get(type).add(c);
		}
		
		List<PlayerCommand> results = new ArrayList<PlayerCommand>();
		for(List<PlayerCommand> group : groups.values()) {
			results.add(merge_group(group));
		}
		return results;
	}

	private PlayerCommand merge_group(List<PlayerCommand> commands) {
		PlayerCommand merged = null;
		for(PlayerCommand c : commands)
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
