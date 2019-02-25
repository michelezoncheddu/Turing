package turing.client;

import javax.swing.*;
import java.net.InetSocketAddress;

public class Client {
	static final String SERVER_NAME = "TURING_SERVER";
	static final String HOST = "localhost";
	static final int DEFAULT_PORT = 1100;
	static final int BACKGROUND_PORT = 1101;
	static final InetSocketAddress DEFAULT_ADDRESS = new InetSocketAddress(HOST, DEFAULT_PORT);
	static final InetSocketAddress BACKGROUND_ADDRESS = new InetSocketAddress(HOST, BACKGROUND_PORT);

	static ClientGUI frame;

	/**
	 * Main function.
	 */
	public static void main(String[] args) { SwingUtilities.invokeLater(() -> frame = new ClientGUI()); }
}
