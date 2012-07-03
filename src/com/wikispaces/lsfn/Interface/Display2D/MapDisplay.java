package com.wikispaces.lsfn.Interface.Display2D;

import com.wikispaces.lsfn.Interface.Model.*;

import java.awt.Dimension;
import javax.swing.JFrame;

@SuppressWarnings("serial") // not intending to serialize UI elements
public class MapDisplay extends JFrame {
	public MapDisplay(KnownSpace world) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(1024, 1024));
		getRootPane().setDoubleBuffered(true);
		getContentPane().add(new SpacePanel(world));
		setVisible(true);
	}
	
	// A registered component signals that the Frame needs repainting.
	public void step () {
		repaint();
	}
}
