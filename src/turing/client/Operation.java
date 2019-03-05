package turing.client;

import org.json.JSONObject;
import turing.Fields;

import javax.swing.*;

public class Operation {
	private static Connection connection;

	/**
	 * Sets the connection
	 */
	public static void setConnection(Connection newConnection) {
		connection = newConnection;
	}

	/**
	 * Checks the reply
	 */
	private static boolean isSuccessful(JSONObject reply) {
		if (reply.get(Fields.STATUS).equals(Fields.STATUS_ERR)) {
			Client.frame.showErrorDialog((String) reply.get(Fields.ERR_MSG));
			return false;
		}
		return true;
	}

	/**
	 * Implements the log in operation
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
		if (!isSuccessful(reply))
			return;

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
		// if (!isSuccessful(reply)) return; // TODO

		Client.frame.clearTables();
		connection.downloadTablesData((Integer) reply.get(Fields.INCOMING_MESSAGES));
	}

	/**
	 * Performs the create document operation
	 */
	public static void createDocument(String documentName, int sections) {
		// create create document request
		JSONObject request = new JSONObject();
		request.put(Fields.OPERATION, Fields.OPERATION_CREATE_DOC)
				.put(Fields.DOCUMENT_NAME, documentName)
				.put(Fields.NUMBER_OF_SECTIONS, sections);

		JSONObject reply = connection.requestReply(request);
		// if (!isSuccessful(reply)) return; // TODO

		Operation.list(); // updating table data
	}

	/**
	 * Performs the edit section operation
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
		// if (!isSuccessful(reply)) return; // TODO

		Client.frame.createEditingSpace((String) reply.get(Fields.SECTION_CONTENT));
	}

	/**
	 * Performs the end edit operation
	 */
	public static void endEdit(String sectionContent) {
		// create end edit request
		JSONObject request = new JSONObject();
		request.put(Fields.OPERATION, Fields.OPERATION_END_EDIT)
				.put(Fields.SECTION_CONTENT, sectionContent);

		JSONObject reply = connection.requestReply(request);
		// if (!isSuccessful(reply)) return; // TODO

		if (reply.get(Fields.STATUS).equals(Fields.STATUS_OK)) { // TODO: check with isSuccessful
			Client.frame.createWorkspace();
			Operation.list();
		}
	}
}
