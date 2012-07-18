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
public class ConnectButtonWrapperPanel extends JPanel implements ActionListener {
	private JButton connect;
	private JTextField host;
	private JTextField port;
	private InterfaceClient client;

	public ConnectButtonWrapperPanel(InterfaceClient client, JPanel wrapped) {
		this.client = client;
		connect = new JButton();
		connect.setText("Connect");
		connect.addActionListener(this);
		
		host = new JTextField(30);
		host.setText("localhost");
		port = new JTextField(5);
		port.setText("14612");
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
	public void actionPerformed(ActionEvent e) {
		client.start_SHIP_client(host.getText(), Integer.parseInt(port.getText()));
		connect.setEnabled(false);
	}
}
