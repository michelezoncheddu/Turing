package turing.client;

import org.json.JSONObject;
import turing.ClientNotificationManagerAPI;
import turing.ServerNotificationManagerAPI;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implements the connection and the communication with the server
 */
public class Connection {

	// streams with the server
	private BufferedWriter writer;
	private BufferedReader reader;

	/**
	 * Initializes the connection with the server
	 *
	 * @param address the server address
	 *
	 * @throws IOException if a network error occurs
	 */
	public Connection(InetSocketAddress address) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		Socket defaultConnection = new Socket();
		defaultConnection.connect(address);
		writer = new BufferedWriter(new OutputStreamWriter(defaultConnection.getOutputStream(), StandardCharsets.UTF_8));
		reader = new BufferedReader(new InputStreamReader(defaultConnection.getInputStream(), StandardCharsets.UTF_8));
	}

	/**
	 * Executes the request/reply protocol
	 *
	 * @param request the message to send
	 * @return the reply message
	 */
	public JSONObject requestReply(JSONObject request) {
		String replyString;

		// send request and wait reply
		try {
			writer.write(request.toString());
			writer.newLine();
			writer.flush();
			replyString = reader.readLine();
		} catch (IOException e) {
			Client.frame.showErrorDialog("Communication error");
			return null;
		}
		if (replyString == null) // server disconnected
			return null;

		return new JSONObject(replyString);
	}

	/**
	 * Registers the client for server notifications
	 */
	public void registerForNotifications(String username, String password) {
		try {
			Registry registry = LocateRegistry.getRegistry(Client.HOST);
			ServerNotificationManagerAPI serverAPI = (ServerNotificationManagerAPI) registry.lookup(Client.NOTIFICATION_OBJECT);
			ClientNotificationManager listener = new ClientNotificationManager();
			ClientNotificationManagerAPI stub = (ClientNotificationManagerAPI) UnicastRemoteObject.exportObject(listener, 0);
			serverAPI.registerForNotifications(username, password, stub);
		} catch (NullPointerException | RemoteException | NotBoundException e) {
			Client.frame.showErrorDialog(e.getMessage());
		}
	}
}
