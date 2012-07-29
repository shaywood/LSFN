package com.wikispaces.lsfn.Interface.Display2D;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.BlockingQueue;

import javax.swing.Action;

import com.wikispaces.lsfn.Shared.Messaging.Message;


public class SwingActionHelper implements Action {

	private Message command;
	private BlockingQueue<Message> player_input_queue;

	public SwingActionHelper(BlockingQueue<Message> player_input_queue, Message command) {
		this.player_input_queue = player_input_queue;
		this.command = command;
	}

	public void actionPerformed(ActionEvent e) {
		player_input_queue.add(command);
	}

	public Object getValue(String key) {
		return null;
	}

	public void putValue(String key, Object value) {
	}

	public void setEnabled(boolean b) {
	}

	public boolean isEnabled() {
		return true;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}
}