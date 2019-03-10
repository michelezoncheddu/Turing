package turing.client;

import org.json.JSONObject;
import turing.Fields;
import turing.UserManagerAPI;

import javax.swing.*;
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
				JOptionPane.showMessageDialog(Client.frame, "Can't register " + username,
						"Error", JOptionPane.ERROR_MESSAGE); // TODO: specify error and exceptions
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
			JOptionPane.showMessageDialog(Client.frame, "Compile all fields");
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

		Client.frame.createWorkspace(); // create the workspace window
		Operation.list();
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
		connection.downloadTablesData((Integer) reply.get(Fields.INCOMING_MESSAGES));
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
			JOptionPane.showMessageDialog(Client.frame, "Select a section");
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

		Client.frame.createEditingSpace((String) reply.get(Fields.SECTION_CONTENT));
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
	 * Performs the send of a chat message
	 *
	 * @param message the message to send
	 */
	public static void sendMessage(String message) {
		// create and edit request
		JSONObject request = new JSONObject();
		request.put(Fields.OPERATION, Fields.OPERATION_CHAT_MSG)
				.put(Fields.CHAT_MSG, message);

		JSONObject reply = connection.requestReply(request);
		if (isErrorMessage(reply))
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
	}
}
