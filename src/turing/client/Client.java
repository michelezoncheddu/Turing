package turing.client;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Main client class with configuration parameters
 */
public class Client {
	static String SERVER_ADDR  = "localhost"; // server address
	static int    DEFAULT_PORT = 1100;        // server socket port
	static int    CHAT_PORT    = 1101;        // multicast port
	static int    MTU          = 1500;        // Ethernet MTU

	// socket address
	static final InetSocketAddress DEFAULT_ADDRESS = new InetSocketAddress(SERVER_ADDR, DEFAULT_PORT);

	// Java RMI objects name
	static String REGISTRATION_OBJECT = "reg";
	static String NOTIFICATION_OBJECT = "not";

	// application frame
	static ClientGUI frame;

	/**
	 * Creates a new client
	 */
	public Client() {
		super();
	}

	/**
	 * Loads the configuration file
	 *
	 * @param fileName the configuration file name
	 */
	private static void loadConfiguration(String fileName) {
		System.out.println("Loading configuration file: " + fileName);
		Properties prop = new Properties();
		InputStream is;
		try {
			is = new FileInputStream(fileName);
			prop.load(is);
		} catch (NullPointerException | IOException e) {
			System.err.println("Cannot find configuration file: " + e.getMessage());
			System.out.println("Loading default configuration");
			return;
		}

		try {
			DEFAULT_PORT   = Integer.parseInt(prop.getProperty("DEFAULT_PORT"));
			CHAT_PORT      = Integer.parseInt(prop.getProperty("CHAT_PORT"));
			MTU            = Integer.parseInt(prop.getProperty("MTU"));
		} catch (NumberFormatException e) {
			System.err.println("Bad configuration file format: " + e.getMessage());
		}

		SERVER_ADDR = prop.getProperty("SERVER_ADDR", SERVER_ADDR);
		REGISTRATION_OBJECT = prop.getProperty("REGISTRATION_OBJECT", REGISTRATION_OBJECT);
		NOTIFICATION_OBJECT = prop.getProperty("NOTIFICATION_OBJECT", NOTIFICATION_OBJECT);
	}

	/**
	 * Starts the client GUI
	 *
	 * @param args the client arguments
	 */
	public static void main(String[] args) {
		if (args.length > 0)
			loadConfiguration(args[0]);
		SwingUtilities.invokeLater(() -> frame = new ClientGUI());
	}
}
