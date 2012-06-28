package com.wikispaces.lsfn.Interface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class SpacePanel extends JPanel {

	public SpacePanel () {
		setBackground(Color.BLACK);
	}

	//Overridden method, called every time the component is repainted.
	public void paintComponent (Graphics drawHere) {
		Graphics2D drawOn = (Graphics2D) drawHere;
		super.paintComponent(drawOn);
		// For each of the types of things that can be inside our space model, we need something that can draw them that we can delegate to here.
		new ShipDot().draw(drawOn);
	}
}
