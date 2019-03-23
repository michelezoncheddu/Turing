package turing.client;

import javax.swing.*;
import java.net.InetSocketAddress;

/**
 * Main client class with configuration parameters
 */
public class Client {
	static final String SERVER_ADDR  = "localhost"; // server address
	static final int    DEFAULT_PORT = 1100;        // server socket port
	static final int    CHAT_PORT    = 1101;        // multicast port
	static final int    MTU          = 1500;        // Ethernet MTU

	// socket address
	static final InetSocketAddress DEFAULT_ADDRESS = new InetSocketAddress(SERVER_ADDR, DEFAULT_PORT);

	// Java RMI objects name
	static final String REGISTRATION_OBJECT = "reg";
	static final String NOTIFICATION_OBJECT = "not";

	// application frame
	static ClientGUI frame;

	/**
	 * Creates a new client
	 */
	public Client() {
		super();
	}

	/**
	 * Starts the client GUI
	 *
	 * @param args the client arguments
	 */
	public static void main(String[] args) { SwingUtilities.invokeLater(() -> frame = new ClientGUI()); }
}
