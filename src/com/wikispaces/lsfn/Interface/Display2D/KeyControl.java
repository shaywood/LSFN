package com.wikispaces.lsfn.Interface.Display2D;

import java.util.HashSet;
import java.util.Set;

import javax.swing.KeyStroke;


import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.Subscription.AccelerateEastWest;
import com.wikispaces.lsfn.Shared.Subscription.AccelerateNorthSouth;
import com.wikispaces.lsfn.Shared.Subscription.Subscribeable;

public enum KeyControl {
	ACCELERATE_FORWARD(new AccelerateNorthSouth(UnitDirection.NORTH), "W", "UP"),
	ACCELERATE_BACKWARD(new AccelerateNorthSouth(UnitDirection.SOUTH), "S", "DOWN"),
	ACCELERATE_PORT(new AccelerateEastWest(UnitDirection.EAST), "A", "LEFT"),
	ACCELERATE_STARBOARD(new AccelerateEastWest(UnitDirection.WEST), "D", "RIGHT");
	
	private Subscribeable command;
	private Set<KeyStroke> bindings = new HashSet<KeyStroke>();

	KeyControl(Subscribeable command, String... default_bindings) {
		this.command = command;
		for(String key_name : default_bindings) {
			try {
				add_key(key_name);
			} catch (NoSuchKeyException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	public String get_action_name() {
		return this.name();
	}
	
	public Subscribeable get_control() {
		return command;
	}

	public Set<KeyStroke> get_key_bindings() {
		return bindings;
	}
	
	private KeyStroke get_key_stroke(String key) throws NoSuchKeyException {
		KeyStroke keyStroke = KeyStroke.getKeyStroke(key);
		if(keyStroke == null) {
			throw new NoSuchKeyException(key);
		}
		return keyStroke;
	}
	
	public void add_key(String new_binding) throws NoSuchKeyException {
		bindings.add(get_key_stroke(new_binding));
	}
	
	public void remove_key(String old_binding) throws NoSuchKeyException {
		bindings.remove(get_key_stroke(old_binding));
	}
	
	public void clear_keys() {
		bindings = new HashSet<KeyStroke>();
	}
}
