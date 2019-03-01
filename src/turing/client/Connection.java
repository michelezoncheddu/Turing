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
 * Implements the connection with the server
 */
public class Connection {
	BufferedWriter writer;
	BufferedReader reader;

	/**
	 * Initializes the connection with the server
	 */
	public Connection(InetSocketAddress defaultAddress, InetSocketAddress backgroundAddress) throws IOException {
		Socket defaultConnection = new Socket();
		Socket backgroundConnection = new Socket();
		defaultConnection.connect(defaultAddress);
		backgroundConnection.connect(backgroundAddress);
		writer = new BufferedWriter(new OutputStreamWriter(defaultConnection.getOutputStream(), StandardCharsets.UTF_8));
		reader = new BufferedReader(new InputStreamReader(defaultConnection.getInputStream(), StandardCharsets.UTF_8));
	}

	/**
	 * Performs the sign up function
	 */
	public void signUp(String username, String password) {
		if (username.isBlank() || password.isBlank())
			return;

		UserManagerAPI serverObject;
		Remote remoteObject;
		try {
			Registry r = LocateRegistry.getRegistry();
			remoteObject = r.lookup(Client.SERVER_NAME);
			serverObject = (UserManagerAPI) remoteObject;
			boolean success = serverObject.signUp(username, password);
			if (success)
				JOptionPane.showMessageDialog(Client.frame, username + " registered");
			else
				JOptionPane.showMessageDialog(Client.frame, "Can't register " + username,
						"Error", JOptionPane.ERROR_MESSAGE); // TODO: specify error
		} catch (RemoteException e) {
			Client.frame.showErrorDialog("Communication error", e);
		} catch (NotBoundException e) {
			Client.frame.showErrorDialog("Unable to find registration service", e);
		}
	}

	/**
	 * Performs the log in function
	 */
	public void logIn(String username, String password) {
		if (username.isBlank() || password.isBlank())
			return;

		// create login request
		JSONObject json = new JSONObject();
		json.put(Fields.OPERATION, Fields.OPERATION_LOGIN);
		json.put(Fields.USERNAME, username);
		json.put(Fields.PASSWORD, password);

		// send login request and wait reply
		String jsonString;
		try {
			json.write(writer);
			writer.newLine();
			writer.flush();
			jsonString = reader.readLine();
		} catch (IOException e) {
			Client.frame.showErrorDialog("Communication error", e);
			return;
		}

		JSONObject reply = new JSONObject(jsonString);
		if (reply.get(Fields.STATUS).equals(Fields.STATUS_OK)) { // logged successfully
			Client.frame.createWorkspace();
			list(); // download table data
		} else
			JOptionPane.showMessageDialog(Client.frame, "Inexistent user or wrong password"); // TODO: specify error
	}

	/**
	 * TO DO
	 */
	public void list() {
		JSONObject req = new JSONObject();
		String jsonString;
		req.put(Fields.OPERATION, Fields.OPERATION_LIST);

		// send message and wait reply
		try {
			req.write(writer);
			writer.newLine();
			writer.flush();
			jsonString = reader.readLine();
		} catch (IOException e) {
			Client.frame.showErrorDialog("Communication error", e);
			return;
		}

		Client.frame.clearTables();

		JSONObject msg = new JSONObject(jsonString);
		int incoming = (Integer) msg.get(Fields.INCOMING_MESSAGES);

		// download table data
		for (int i = 0; i < incoming; i++) {
			try {
				jsonString = reader.readLine();
			} catch (IOException e) {
				Client.frame.showErrorDialog("Communication error", e);
			}
			JSONObject jsonDoc = new JSONObject(jsonString);
			Document doc = new Document((String) jsonDoc.get("name"),
					(String) jsonDoc.get("creator"), (Integer) jsonDoc.get("sections"));
			Client.frame.addDocument(doc);
		}
	}

	/**
	 * TO DO
	 */
	public void createDocument(String documentName, int sections) {
		JSONObject req = new JSONObject();
		String jsonString;
		req.put(Fields.OPERATION, Fields.OPERATION_CREATE_DOC);
		req.put(Fields.DOCUMENT_NAME, documentName);
		req.put(Fields.NUMBER_OF_SECTIONS, sections);

		// send message and wait reply
		try {
			req.write(writer);
			writer.newLine();
			writer.flush();
			jsonString = reader.readLine();
		} catch (IOException e) {
			Client.frame.showErrorDialog("Communication error", e);
			return;
		}
		JSONObject reply = new JSONObject(jsonString);
		if (reply.get(Fields.STATUS).equals(Fields.STATUS_OK))
			list(); // download table data
		else
			JOptionPane.showMessageDialog(Client.frame, "Error creating document");

		// *** TEST
		/*JSONObject tmp = new JSONObject();
		tmp.put(Fields.OPERATION, Fields.OPERATION_EDIT_SECTION);
		tmp.put(Fields.DOCUMENT_CREATOR, "admin");
		tmp.put(Fields.DOCUMENT_NAME, "test");
		tmp.put(Fields.DOCUMENT_SECTION, 0);
		tmp.write(writer);
		try {
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		// *** TEST
	}

	/**
	 * TO DO
	 */
	public void editSection(Document document, int section) {
		if (section < 0) {
			JOptionPane.showMessageDialog(Client.frame, "Select a section");
			return;
		}
		JSONObject req = new JSONObject();
		String jsonString;
		req.put(Fields.OPERATION, Fields.OPERATION_EDIT_SECTION);
		req.put(Fields.DOCUMENT_NAME, document.getName());
		req.put(Fields.DOCUMENT_CREATOR, document.getCreator());
		req.put(Fields.DOCUMENT_SECTION, section + 1);

		// send request and wait reply
		try {
			req.write(writer);
			writer.newLine();
			writer.flush();
			jsonString = reader.readLine();
		} catch (IOException e) {
			Client.frame.showErrorDialog("Communication error", e);
			return;
		}
		JOptionPane.showMessageDialog(Client.frame, section + 1);
	}
}
