package com.wikispaces.lsfn.Interface.Display2D;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.wikispaces.lsfn.Interface.InterfaceClient;

@SuppressWarnings("serial")
public abstract class ConnectButtonWrapperPanel extends JPanel implements ActionListener {
	protected JButton connect;
	protected JTextField host;
	protected JTextField port;
	protected InterfaceClient client;

	public ConnectButtonWrapperPanel(InterfaceClient client, JPanel wrapped, Integer default_port, String button_text) {
		this.client = client;
		connect = new JButton();
		connect.setText(button_text);
		connect.addActionListener(this);
		
		host = new JTextField(30);
		host.setText("localhost");
		port = new JTextField(5);
		port.setText(default_port.toString());
		JPanel button_row = new JPanel();
		button_row.add(connect);
		button_row.add(host);
		button_row.add(port);
		button_row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(button_row);
		this.add(wrapped);
	}
	

	@Override
	public abstract void actionPerformed(ActionEvent e);
}
