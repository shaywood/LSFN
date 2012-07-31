package com.wikispaces.lsfn.Shared.Helpers;

import java.util.Arrays;
import java.util.Iterator;

public class StringHelpers {
	public static String join (String[] strings, String join_on) {
		return join(Arrays.asList(strings), join_on);
	}
	
	public static String join (Iterable<? extends Object> strings, String join_on) {
		StringBuilder builder = new StringBuilder();
		Iterator<? extends Object> it = strings.iterator();
		
		if(it.hasNext()) {
			builder.append(it.next());
		}
		
		while(it.hasNext()) {
			builder.append(join_on);
			builder.append(it.next());
		}
		return builder.toString();
	}
}
