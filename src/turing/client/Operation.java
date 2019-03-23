package turing.client;

import org.json.JSONArray;
import org.json.JSONException;
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
public abstract class Operation {
	private static Connection connection; // connection with the server

	/**
	 * Creates a new operation class
	 */
	private Operation() {
		super();
	}

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
	 *
	 * @param message the message to check
	 *
	 * @return true if the message contains an error
	 *         fale otherwise
	 */
	private static boolean isErrorMessage(JSONObject message) {
		return message.get(Fields.STATUS).equals(Fields.STATUS_ERR);
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
			Registry registry = LocateRegistry.getRegistry(Client.SERVER_ADDR);
			remoteObject = registry.lookup(Client.REGISTRATION_OBJECT);
			serverObject = (UserManagerAPI) remoteObject;

			// trying to register user
			boolean success = serverObject.signUp(username, password);
			if (success)
				Client.frame.showInfoDialog(username + " registered");
			else
				Client.frame.showErrorDialog("Username already in use: " + username);
		} catch (NullPointerException | IllegalArgumentException e) {
			Client.frame.showErrorDialog(e.getMessage());
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
		request.put(Fields.OP, Fields.OP_LOGIN)
				.put(Fields.USERNAME, username)
				.put(Fields.PASSWORD, password);

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return;
		}

		connection.registerForNotifications(username, password);
		Client.frame.username = username;
		Client.frame.showWorkspace(); // create the workspace window
		list(); // download table data from server
	}

	/**
	 * Performs the logout operation
	 */
	public static void logout() {
		// create a logout request
		JSONObject request = new JSONObject();
		request.put(Fields.OP, Fields.OP_LOGOUT);

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply))
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));

		Client.frame.showLoginWindow();
	}

	/**
	 * Performs the create document operation
	 *
	 * @param documentName name of the document to create
	 * @param sections     number of sections of the document
	 */
	public static void createDocument(String documentName, int sections) {
		// create a create document request
		JSONObject request = new JSONObject();
		request.put(Fields.OP, Fields.OP_CREATE_DOC)
				.put(Fields.DOC_NAME, documentName)
				.put(Fields.SECTIONS, sections);

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return;
		}

		list(); // updating table data
	}

	/**
	 * Performs the show document operation
	 *
	 * @param document the document to show
	 */
	public static void showDocument(Document document) {
		if (document == null) {
			Client.frame.showErrorDialog("Select a document");
			return;
		}

		// create show document request
		JSONObject request = new JSONObject();
		request.put(Fields.OP, Fields.OP_SHOW_DOC)
				.put(Fields.DOC_NAME, document.getName())
				.put(Fields.DOC_CREATOR, document.getCreator());

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return;
		}

		Client.frame.showDocumentWindow((String) reply.get(Fields.DOC_CONTENT));
	}

	/**
	 * Performs the show section operation
	 *
	 * @param document the document
	 * @param section  the document section to show
	 */
	public static void showSection(Document document, int section) {
		if (section < 0) {
			Client.frame.showErrorDialog("Select a section");
			return;
		}

		// create show section request
		JSONObject request = new JSONObject();
		request.put(Fields.OP, Fields.OP_SHOW_SEC)
				.put(Fields.DOC_NAME, document.getName())
				.put(Fields.DOC_CREATOR, document.getCreator())
				.put(Fields.DOC_SECTION, section);

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return;
		}

		Client.frame.showDocumentWindow((String) reply.get(Fields.SEC_CONTENT));
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
		request.put(Fields.OP, Fields.OP_EDIT_SEC)
				.put(Fields.DOC_NAME, document.getName())
				.put(Fields.DOC_CREATOR, document.getCreator())
				.put(Fields.DOC_SECTION, section);

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return;
		}

		InetAddress chatAddress = null;
		try {
			chatAddress = InetAddress.getByName((String) reply.get(Fields.CHAT_ADDR));
		} catch (JSONException | UnknownHostException e) {
			Client.frame.showErrorDialog("Chat unavailable");
		}
		Client.frame.showEditingWindow((String) reply.get(Fields.SEC_CONTENT), chatAddress);
	}

	/**
	 * Performs the end edit operation
	 *
	 * @param sectionContent the new section content
	 */
	public static void endEdit(String sectionContent) {
		// create end edit request
		JSONObject request = new JSONObject();
		request.put(Fields.OP, Fields.OP_END_EDIT)
				.put(Fields.SEC_CONTENT, sectionContent);

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return;
		}

		Client.frame.showWorkspace();
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
		request.put(Fields.OP, Fields.OP_INVITE)
				.put(Fields.USERNAME, username)
				.put(Fields.DOC_NAME, document.getName())
				.put(Fields.DOC_CREATOR, document.getCreator());

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply))
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
		else {
			Client.frame.showInfoDialog(username + " invited");
			document.setShared(true);
			Client.frame.updateDocumentsTable();
		}
	}

	/**
	 * Performs the list operation
	 */
	public static void list() {
		// create list request
		JSONObject request = new JSONObject();
		request.put(Fields.OP, Fields.OP_LIST);

		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return;
		}

		Client.frame.clearWorkspace();

		// downloading documents metadata
		JSONArray docArray = reply.getJSONArray(Fields.DOCS);
		for (int i = 0; i < docArray.length(); i++) {
			Client.frame.addDocument(new Document(
					(String)  docArray.optJSONObject(i).get(Fields.DOC_NAME),
					(String)  docArray.optJSONObject(i).get(Fields.DOC_CREATOR),
					(Integer) docArray.optJSONObject(i).get(Fields.SECTIONS),
					(Boolean) docArray.optJSONObject(i).get(Fields.IS_SHARED)));
		}
	}

	/**
	 * Performs the send of a chat message
	 *
	 * @param textField the chat message field
	 */
	public static void sendMessage(JTextField textField) {
		String message = textField.getText();
		if (message.isBlank())
			return;

		// create and edit request
		JSONObject request = new JSONObject();
		request.put(Fields.OP, Fields.OP_CHAT_MSG)
				.put(Fields.CHAT_MSG, message);

		textField.setText("");
		JSONObject reply = connection.requestReply(request);

		if (isErrorMessage(reply))
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
	}
}
