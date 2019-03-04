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
			Client.frame.showErrorDialog("Communication error", e);
		} catch (NotBoundException e) {
			Client.frame.showErrorDialog("Unable to find registration service", e);
		}
	}

	/**
	 * Performs the log in operation
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
			Client.frame.createWorkspace(); // create the workspace window
			list(); // download table data
		} else
			JOptionPane.showMessageDialog(Client.frame, "Inexistent user or wrong password"); // TODO: specify error
	}

	/**
	 * Performs the list operation
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
	 * Performs the create document operation
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
			list(); // updating table data
		else
			JOptionPane.showMessageDialog(Client.frame, "Error creating document");
	}

	/**
	 * Performs the edit section operation
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
		req.put(Fields.DOCUMENT_SECTION, section);

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
		JSONObject reply = new JSONObject(jsonString);
		Client.frame.createEditingSpace((String) reply.get(Fields.SECTION_CONTENT));
	}

	/**
	 * Performs the end edit operation
	 */
	public void endEdit(Document document, int section, String sectionContent) {
		JSONObject req = new JSONObject();
		String jsonString;
		req.put(Fields.OPERATION, Fields.OPERATION_END_EDIT);
		req.put(Fields.DOCUMENT_NAME, document.getName());
		req.put(Fields.DOCUMENT_CREATOR, document.getCreator());
		req.put(Fields.DOCUMENT_SECTION, section);
		req.put(Fields.SECTION_CONTENT, sectionContent);

		// send request and TODO: wait reply
		try {
			req.write(writer);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
