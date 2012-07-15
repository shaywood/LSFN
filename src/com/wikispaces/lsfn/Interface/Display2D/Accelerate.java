package com.wikispaces.lsfn.Interface.Display2D;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

public class Accelerate implements Action {

	private Direction where_to;

	public Accelerate(Direction where_to) {
		this.where_to = where_to;
	}
	
	public void actionPerformed(ActionEvent e) {
		System.out.println("Accelerating  " + where_to);
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
