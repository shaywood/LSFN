package com.wikispaces.lsfn.Interface.Display2D;

import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.KeyStroke;

public enum Controls implements KeyControl {
	ACCELERATE_FORWARD(new Accelerate(Direction.BOW), "W", "UP"),
	ACCELERATE_BACKWARD(new Accelerate(Direction.STERN), "S", "DOWN"),
	ACCELERATE_PORT(new Accelerate(Direction.PORT), "A", "LEFT"),
	ACCELERATE_STARBOARD(new Accelerate(Direction.STARBOARD), "D", "RIGHT");
	
	private Action action;
	private Set<KeyStroke> bindings = new HashSet<KeyStroke>();

	Controls(Action action, String... default_bindings) {
		this.action = action;
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
	
	public Action get_action() {
		return action;
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
