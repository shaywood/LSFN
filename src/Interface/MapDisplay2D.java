package com.wikispaces.lsfn.Interface;

import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.Dimension;
import javax.swing.JFrame;


public class MapDisplay2D extends JFrame {

	public MapDisplay2D() {
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
