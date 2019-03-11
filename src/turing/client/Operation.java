package turing.client;

import org.json.JSONArray;
import org.json.JSONObject;
import turing.Fields;
import turing.UserManagerAPI;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Class that performs the client operations
 */
public class Operation {
	private static Connection connection; // connection with the server

	/**
	 * Sets the connection
	 *
	 * @param newConnection connection with the server
	 */
	public static void setConnection(Connection newConnection) {
		connection = newConnection;
	}

	/**
	 * Checks if the reply is an error message
	 */
	private static boolean isErrorMessage(JSONObject reply) {
		return reply.get(Fields.STATUS).equals(Fields.STATUS_ERR);
	}

	/**
	 * Performs the sign up operation
	 *
	 * @param username username to register
	 * @param password user's password
	 */
	public static void signUp(String username, String password) {
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
				Client.frame.showErrorDialog("Can't register " + username); // TODO: specify error and exceptions
		} catch (RemoteException e) {
			Client.frame.showErrorDialog("Communication error");
		} catch (NotBoundException e) {
			Client.frame.showErrorDialog("Unable to find registration service");
		}
	}

	/**
	 * Performs the log in operation
	 *
	 * @param username username to log in
	 * @param password user's password
	 */
	public static void logIn(String username, String password) {
		if (username.isBlank() || password.isBlank()) {
			Client.frame.showErrorDialog("Compile all fields");
			return;
		}

		// create login request
		JSONObject request = new JSONObject();
		request.put(Fields.OPERATION, Fields.OPERATION_LOGIN)
				.put(Fields.USERNAME, username)
				.put(Fields.PASSWORD, password);

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return;
		}

		connection.registerForNotifications(username, password);
		Client.frame.createWorkspace(); // create the workspace window
		Operation.list();
	}

	/**
	 * Performs the create document operation
	 *
	 * @param documentName name of the document to create
	 * @param sections     number of sections of the document
	 */
	public static void createDocument(String documentName, int sections) {
		// create create document request
		JSONObject request = new JSONObject();
		request.put(Fields.OPERATION, Fields.OPERATION_CREATE_DOC)
				.put(Fields.DOCUMENT_NAME, documentName)
				.put(Fields.NUMBER_OF_SECTIONS, sections);

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return;
		}

		Operation.list(); // updating table data
	}

	/**
	 * Performs the edit section operation
	 *
	 * @param document document to edit
	 * @param section  index section to edit
	 */
	public static void editSection(Document document, int section) {
		if (section < 0) {
			Client.frame.showErrorDialog("Select a section");
			return;
		}

		// create edit section request
		JSONObject request = new JSONObject();
		request.put(Fields.OPERATION, Fields.OPERATION_EDIT_SECTION)
				.put(Fields.DOCUMENT_NAME, document.getName())
				.put(Fields.DOCUMENT_CREATOR, document.getCreator())
				.put(Fields.DOCUMENT_SECTION, section);

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return;
		}

		InetAddress chatAddress = null;
		try {
			chatAddress = InetAddress.getByName((String) reply.get(Fields.CHAT_ADDRESS));
		} catch (UnknownHostException e) {
			Client.frame.showErrorDialog("Chat unavailable");
		}
		Client.frame.createEditingWindow((String) reply.get(Fields.SECTION_CONTENT), chatAddress);
	}

	/**
	 * Performs the end edit operation
	 *
	 * @param sectionContent the new section content
	 */
	public static void endEdit(String sectionContent) {
		// create end edit request
		JSONObject request = new JSONObject();
		request.put(Fields.OPERATION, Fields.OPERATION_END_EDIT)
				.put(Fields.SECTION_CONTENT, sectionContent);

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return;
		}

		Client.frame.createWorkspace();
		Operation.list();
	}

	/**
	 * Performs the invite operation
	 *
	 * @param username the user to invite
	 * @param document the document to share with the user
	 */
	public static void invite(String username, Document document) {
		// create invite request
		JSONObject request = new JSONObject();
		request.put(Fields.OPERATION, Fields.OPERATION_INVITE)
				.put(Fields.USERNAME, username)
				.put(Fields.DOCUMENT_NAME, document.getName())
				.put(Fields.DOCUMENT_CREATOR, document.getCreator());

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply))
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
		else
			JOptionPane.showMessageDialog(Client.frame, username + " invited"); // TODO: set "shared" to "yes"
	}

	/**
	 * Performs the list operation
	 */
	public static void list() {
		// create list request
		JSONObject request = new JSONObject();
		request.put(Fields.OPERATION, Fields.OPERATION_LIST);

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return;
		}

		Client.frame.clearTables();

		// downloading documents metadata
		JSONArray docArray = reply.getJSONArray(Fields.DOCUMENTS);
		for (int i = 0; i < docArray.length(); i++) {
			Client.frame.addDocument(new Document(
					(String)  docArray.optJSONObject(i).get(Fields.DOCUMENT_NAME),
					(String)  docArray.optJSONObject(i).get(Fields.DOCUMENT_CREATOR),
					(Integer) docArray.optJSONObject(i).get(Fields.NUMBER_OF_SECTIONS),
					(Boolean) docArray.optJSONObject(i).get(Fields.IS_SHARED)));
		}
	}

	/**
	 * Performs the send of a chat message
	 *
	 * @param textField the chat message field
	 */
	public static void sendMessage(JTextField textField) {
		// create and edit request
		JSONObject request = new JSONObject();
		request.put(Fields.OPERATION, Fields.OPERATION_CHAT_MSG)
				.put(Fields.CHAT_MSG, textField.getText());

		textField.setText("");
		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply))
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
	}
}
