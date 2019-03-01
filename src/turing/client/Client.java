package turing.client;

import javax.swing.*;
import java.net.InetSocketAddress;

public class Client {
	static final String REGISTRATION_OBJECT = "reg";
	static final String NOTIFICATION_OBJECT = "not";
	static final String HOST                = "localhost";
	static final int    DEFAULT_PORT        = 1100;
	static final InetSocketAddress DEFAULT_ADDRESS = new InetSocketAddress(HOST, DEFAULT_PORT);

	static ClientGUI frame;

	/**
	 * Main function.
	 */
	public static void main(String[] args) { SwingUtilities.invokeLater(() -> frame = new ClientGUI()); }
}
