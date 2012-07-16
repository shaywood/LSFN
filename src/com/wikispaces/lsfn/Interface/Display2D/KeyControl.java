package com.wikispaces.lsfn.Interface.Display2D;

import java.util.Set;

import javax.swing.KeyStroke;

import com.wikispaces.lsfn.Interface.PlayerCommand;

public interface KeyControl {
	String get_action_name();
	PlayerCommand get_control();
	Set<KeyStroke> get_key_bindings();
	void add_key(String new_binding) throws NoSuchKeyException;
	void remove_key(String old_binding) throws NoSuchKeyException;
	void clear_keys();
}
