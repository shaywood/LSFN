package com.wikispaces.lsfn.Interface.Display2D;

import java.util.Set;

import javax.swing.Action;
import javax.swing.KeyStroke;

public interface KeyControl {
	String get_action_name();
	Action get_action();
	Set<KeyStroke> get_key_bindings();
	void add_key(String new_binding) throws NoSuchKeyException;
	void remove_key(String old_binding) throws NoSuchKeyException;
	void clear_keys();
}
