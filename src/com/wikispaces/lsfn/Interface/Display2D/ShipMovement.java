package com.wikispaces.lsfn.Interface.Display2D;

import java.util.concurrent.BlockingQueue;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.wikispaces.lsfn.Interface.PlayerCommand;

public class ShipMovement {
	private InputMap input_map;
	private ActionMap action_map;
	private BlockingQueue<PlayerCommand> player_input_queue;

	public ShipMovement(BlockingQueue<PlayerCommand> player_input_queue, JComponent view_around_ship, Iterable<? extends KeyControl> bindings) {
		this.player_input_queue = player_input_queue;
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
		action_map.put(binding.get_action_name(), new SwingActionHelper(player_input_queue, binding.get_control()));
	}
}
