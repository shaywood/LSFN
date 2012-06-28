package com.wikispaces.lsfn.Interface.Display2D;

import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.Dimension;
import javax.swing.JFrame;


public class MapDisplay extends JFrame {

	public MapDisplay() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setPreferredSize(new Dimension(128, 128));
		getRootPane().setDoubleBuffered(true);
		getContentPane().add(new SpacePanel());
		setVisible(true);
	}
	
	// A registered component signals that the Frame needs repainting.
	public void step () {
		repaint();
	}
}
