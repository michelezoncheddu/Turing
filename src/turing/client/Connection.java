package turing.client;

import turing.Fields;
import turing.UserManagerAPI;

import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Implements the connection and the operations with the server
 */
public class Connection {

	// streams with the server
	private BufferedWriter writer;
	private BufferedReader reader;

	/**
	 * Initializes the connection with the server
	 */
	public Connection(InetSocketAddress defaultAddress) throws IOException {
		Socket defaultConnection = new Socket();
		defaultConnection.connect(defaultAddress);
		writer = new BufferedWriter(new OutputStreamWriter(defaultConnection.getOutputStream(), StandardCharsets.UTF_8));
		reader = new BufferedReader(new InputStreamReader(defaultConnection.getInputStream(), StandardCharsets.UTF_8));
	}

	/**
	 * Performs the sign up operation
	 */
	public void signUp(String username, String password) {
		if (username.isBlank() || password.isBlank())
			return;

		// Remote Method Invocation
		UserManagerAPI serverObject;
		Remote remoteObject;
		try {
			// obtaining the remote object
			Registry registry = LocateRegistry.getRegistry(Client.HOST);
			remoteObject = registry.lookup(Client.REGISTRATION_OBJECT);
			serverObject = (UserManagerAPI) remoteObject;

			// trying to register user
			boolean success = serverObject.signUp(username, password);
			if (success)
				JOptionPane.showMessageDialog(Client.frame, username + " registered");
			else
				JOptionPane.showMessageDialog(Client.frame, "Can't register " + username,
						"Error", JOptionPane.ERROR_MESSAGE); // TODO: specify error and exceptions
		} catch (RemoteException e) {
			Client.frame.showErrorDialog("Communication error");
		} catch (NotBoundException e) {
			Client.frame.showErrorDialog("Unable to find registration service");
		}
	}

	/**
	 * Executes the request/reply protocol
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
		return new JSONObject(replyString);
	}

	/**
	 * Downloads the documents table data
	 */
	public void downloadTablesData(Integer incomingMessages) {
		String messageString;
		JSONObject jsonDoc;
		for (int i = 0; i < incomingMessages; i++) {
			try {
				messageString = reader.readLine();
			} catch (IOException e) {
				Client.frame.showErrorDialog("Communication error");
				return;
			}
			jsonDoc = new JSONObject(messageString);
			Client.frame.addDocument(new Document(
					(String) jsonDoc.get(Fields.DOCUMENT_NAME),
					(String) jsonDoc.get(Fields.DOCUMENT_CREATOR),
					(Integer) jsonDoc.get(Fields.NUMBER_OF_SECTIONS),
					(Boolean) jsonDoc.get(Fields.IS_SHARED)));
		}
	}
}
