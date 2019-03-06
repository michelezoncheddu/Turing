package turing.client;

import javax.swing.*;
import java.net.InetSocketAddress;

/**
 * Main client class with configuration parameters
 */
public class Client {
	static final String REGISTRATION_OBJECT = "reg";
	static final String NOTIFICATION_OBJECT = "not";
	static final String HOST                = "localhost";
	static final int    DEFAULT_PORT        = 1100;
	static final int    CHAT_PORT           = 1101;
	static final InetSocketAddress DEFAULT_ADDRESS = new InetSocketAddress(HOST, DEFAULT_PORT);

	// application frame
	static ClientGUI frame;

	/**
	 * Starts the client GUI
	 */
	public static void main(String[] args) { SwingUtilities.invokeLater(() -> frame = new ClientGUI()); }
}
