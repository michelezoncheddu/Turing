package turing.client;

import javax.swing.*;
import java.net.InetSocketAddress;

public class Client {
	public static final String SERVER_NAME = "TURING_SERVER";
	public static final String HOST = "localhost";
	public static final int DEFAULT_PORT = 1100;
	public static final int BACKGROUND_PORT = 1101;
	public static final InetSocketAddress DEFAULT_ADDRESS = new InetSocketAddress(HOST, DEFAULT_PORT);
	public static final InetSocketAddress BACKGROUND_ADDRESS = new InetSocketAddress(HOST, BACKGROUND_PORT);

	public static ClientGUI frame;

	/**
	 * Main function.
	 */
	public static void main(String[] args) { SwingUtilities.invokeLater(() -> frame = new ClientGUI()); }
}
