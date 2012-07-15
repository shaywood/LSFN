package com.wikispaces.lsfn.Interface.Display2D;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class ShipMovement {
	private InputMap input_map;
	private ActionMap action_map;

	public ShipMovement(JComponent view_around_ship, Iterable<? extends KeyControl> bindings) {
		input_map = view_around_ship.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		action_map = view_around_ship.getActionMap();
		
		for(KeyControl k : bindings) {
			bind(k);
		}
	}
	
	private void bind(KeyControl binding) {
		for(KeyStroke k : binding.get_key_bindings()) {
			input_map.put(k,  binding.get_action_name());
		}
		action_map.put(binding.get_action_name(), binding.get_action());
	}
}
