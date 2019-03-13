package turing.server;

import org.json.JSONArray;
import org.json.JSONException;
import turing.Fields;

import org.json.JSONObject;
import turing.server.exceptions.InexistentDocumentException;
import turing.server.exceptions.PreExistentDocumentException;
import turing.server.exceptions.UserNotAllowedException;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.List;

import static java.lang.System.out;

/**
 * A thread that implements the operations of a individual client
 */
public class ClientHandler implements Runnable {
	private Socket clientConnection; // connection with the client
	private BufferedWriter writer;   // output stream with the client
	private User currentUser = null; // currently logged user

	/**
	 * Creates a new client handler with a connection with a client
	 *
	 * @param clientConnection the socket for the client communication
	 */
	public ClientHandler(Socket clientConnection) {
		this.clientConnection = clientConnection;
	}

	/**
	 * Client handling loop
	 */
	@Override
	public void run() {
		BufferedReader reader; // input stream with the client

		// open streams
		try {
			reader = new BufferedReader(new InputStreamReader(clientConnection.getInputStream(), StandardCharsets.UTF_8));
			writer = new BufferedWriter(new OutputStreamWriter(clientConnection.getOutputStream(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace(); // communication error with the client
			return;
		}

		while (true) {
			// read request
			String requestString;
			try {
				requestString = reader.readLine();
			} catch (IOException e) { // communication error with the client
				logout();
				e.printStackTrace();
				break;
			}

			// client disconnected
			if (requestString == null) {
				logout();
				try {
					reader.close();
					writer.close();
					clientConnection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out.println("Client disconnected");
				break; // terminate thread
			}

			// parsing request
			JSONObject request;
			try {
				request = new JSONObject(requestString);
			} catch (JSONException e) {
				sendError(e.getMessage());
				continue;
			}

			// validating request
			if (!isValid(request)) {
				sendError("Bad request format");
				continue;
			}

			handleOperation(request);
		}

		// terminating thread
		try {
			reader.close();
			writer.close();
			clientConnection.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.println("Handler " + Thread.currentThread() + " terminated");
		}
	}

	/**
	 * Checks if the message request is valid
	 *
	 * @param request the message request to validate
	 *
	 * @return true if the request contains all the needed fields
	 *         false otherwise
	 */
	private static boolean isValid(JSONObject request) {
		if (!request.has(Fields.OP) || !(request.get(Fields.OP) instanceof String))
			return false;

		switch ((String) request.get(Fields.OP)) {
		case Fields.OP_LOGIN:
			return request.has(Fields.USERNAME) && request.has(Fields.PASSWORD);

		case Fields.OP_CREATE_DOC:
			return request.has(Fields.DOC_NAME) && request.has(Fields.SECTIONS);

		case Fields.OP_SHOW_DOC:
			return request.has(Fields.DOC_NAME) && request.has(Fields.DOC_CREATOR);

		case Fields.OP_SHOW_SEC:
			return request.has(Fields.DOC_NAME) && request.has(Fields.DOC_CREATOR) &&
					request.has(Fields.DOC_SECTION);

		case Fields.OP_EDIT_SEC:
		 return request.has(Fields.DOC_CREATOR) && request.has(Fields.DOC_NAME) &&
				 request.has(Fields.DOC_SECTION);

		case Fields.OP_INVITE:
		return request.has(Fields.USERNAME) && request.has(Fields.DOC_CREATOR) &&
				request.has(Fields.DOC_NAME);

		case Fields.OP_CHAT_MSG:
			return request.has(Fields.CHAT_MSG);

		default:
			return true; // no fields needed
		}
	}

	private void handleOperation(JSONObject request) {
		String operation = (String) request.get(Fields.OP);
		if (!operation.equals(Fields.OP_LOGIN)) {
			if (currentUser == null || !currentUser.isOnline()) {
				sendError("You must be logged to request this operation");
				return;
			}
		}

		switch (operation) {
		case Fields.OP_LOGIN:
			login(request);
			break;

		case Fields.OP_LOGOUT:
			logout();
			break;

		case Fields.OP_CREATE_DOC:
			createDocument(request);
			break;

		case Fields.OP_SHOW_DOC:
			showDocument(request);
			break;

		case Fields.OP_SHOW_SEC:
			showSection(request);
			break;

		case Fields.OP_EDIT_SEC:
			editSection(request);
			break;

		case Fields.OP_END_EDIT:
			endEdit(request);
			break;

		case Fields.OP_INVITE:
			invite(request);
			break;

		case Fields.OP_LIST:
			list();
			break;

		case Fields.OP_CHAT_MSG:
			chatMessage(request);
			break;

		default:
			sendError("Unknown opeation: " + operation);
		}
	}

	/**
	 * Sends a ack message to the client
	 */
	private void sendAck() {
		JSONObject message = new JSONObject();
		message.put(Fields.STATUS, Fields.STATUS_OK);
		try {
			writer.write(message.toString());
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a message to the client
	 *
	 * @param message the message to send
	 */
	private void sendMessage(JSONObject message) {
		try {
			writer.write(message.toString());
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a error message to the client
	 *
	 * @param msg the explanation of the error to be sent
	 */
	private void sendError(String msg) {
		JSONObject message = new JSONObject();
		message.put(Fields.STATUS, Fields.STATUS_ERR)
				.put(Fields.ERR_MSG, msg);
		try {
			writer.write(message.toString());
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Implements the login operation
	 */
	private void login(JSONObject request) {
		// parsing request
		String username = (String) request.get(Fields.USERNAME);
		String password = (String) request.get(Fields.PASSWORD);

		// try to log user
		if ((currentUser = Server.userManager.logIn(username, password)) != null) {
			sendAck();
			out.println(currentUser.getUsername() + " connected");
		} else {
			sendError("Can't connect " + username); // TODO: specify error
			out.println("Can't connect " + username);
		}
	}

	/**
	 * Implements the logout operation
	 */
	private void logout() {
		if (currentUser == null)
			return;

		currentUser.setOnline(false);

		// release possible locks
		Section currentSection = currentUser.getEditingSection();
		if (currentSection != null) {
			try {
				currentSection.endEdit(currentUser, null);
			} catch (IOException e) {
				e.printStackTrace(); // disk error
			}
		}
		currentUser.setEditingSection(null);
		currentUser.setNotifier(null);
		currentUser = null;
	}

	/**
	 * Implements the create document operation
	 *
	 * @param request the client request
	 */
	private void createDocument(JSONObject request) {
		// parsing request
		String docName = (String) request.get(Fields.DOC_NAME);
		int sections = (Integer) request.get(Fields.SECTIONS);

		// creating the new document
		Document newDoc;
		try {
			newDoc = new Document(docName, currentUser, sections);
		} catch (PreExistentDocumentException e) {
			sendError("Document already created");
			return;
		} catch (IOException e) { // disk error
			sendError(e.getMessage());
			return;
		}
		currentUser.addDocument(newDoc);
		DocumentManager.put(newDoc);
		sendAck();
	}

	/**
	 * Implements the show document operation
	 *
	 * @param request the client request
	 */
	private void showDocument(JSONObject request) {
		// parsing request
		String docName    = (String)  request.get(Fields.DOC_NAME);
		String creator    = (String)  request.get(Fields.DOC_CREATOR);

		Document document = getDocument(docName, creator);
		if (document == null)
			return;

		Section section;
		int sectionNumber = 0;
		StringBuilder content = new StringBuilder();
		while ((section = document.getSection(sectionNumber)) != null) {
			try {
				content.append(section.getContent());
			} catch (IOException e) { // disk error
				sendError(e.getMessage());
				return;
			}
			sectionNumber++;
		}

		JSONObject reply = new JSONObject();
		reply.put(Fields.STATUS, Fields.STATUS_OK)
				.put(Fields.DOC_CONTENT, content.toString());
		sendMessage(reply);
	}

	/**
	 * Implements the show section operation
	 *
	 * @param request the client request
	 */
	private void showSection(JSONObject request) {
		// parsing request
		String docName    = (String)  request.get(Fields.DOC_NAME);
		String creator    = (String)  request.get(Fields.DOC_CREATOR);
		int sectionNumber = (Integer) request.get(Fields.DOC_SECTION);

		Document document = getDocument(docName, creator);
		if (document == null)
			return;

		Section section = document.getSection(sectionNumber);
		if (section == null) {
			sendError("Inexistent section: " + sectionNumber);
			return;
		}

		String content;
		try {
			content = section.getContent();
		} catch (IOException e) { // disk error
			sendError(e.getMessage());
			return;
		}

		JSONObject reply = new JSONObject();
		reply.put(Fields.STATUS, Fields.STATUS_OK)
				.put(Fields.SEC_CONTENT, content);
		sendMessage(reply);
	}

	/**
	 * Implements the edit section operation
	 *
	 * @param request the client request
	 */
	private void editSection(JSONObject request) {
		if (currentUser.getEditingSection() != null) {
			sendError("You're already editing a section");
			return;
		}

		// parsing request
		String docName    = (String)  request.get(Fields.DOC_NAME);
		String creator    = (String)  request.get(Fields.DOC_CREATOR);
		int sectionNumber = (Integer) request.get(Fields.DOC_SECTION);

		Document document = getDocument(docName, creator);
		if (document == null)
			return;

		Section section = document.getSection(sectionNumber);
		if (section == null) {
			sendError("Inexistent section: " + sectionNumber);
			return;
		}

		// check if section is unlocked
		if (section.startEdit(currentUser)) {
			currentUser.setEditingSection(section); // lock user

			// send section content
			String content;
			try {
				content = section.getContent();
			} catch (IOException e) { // disk error
				sendError(e.getMessage());
				return;
			}
			JSONObject reply = new JSONObject();
			reply.put(Fields.STATUS, Fields.STATUS_OK)
					.put(Fields.SEC_CONTENT, content)
					.put(Fields.CHAT_ADDR, document.getChatAddress().getHostAddress());
			sendMessage(reply);
		} else {
			sendError("Another user is editing this section");
		}
	}

	/**
	 * Implements the end edit operation
	 *
	 * @param request the client request
	 */
	private void endEdit(JSONObject request) {
		Section section = currentUser.getEditingSection();

		// user isn't editing any section
		if (section == null) {
			sendError("You're not editing any section");
			return;
		}

		// new section content
		String content = request.has(Fields.SEC_CONTENT) ? (String) request.get(Fields.SEC_CONTENT) : null;

		// unlock section
		try {
			section.endEdit(currentUser, content);
		} catch (IOException e) {
			sendError(e.getMessage());
		}
		currentUser.setEditingSection(null); // unlock user
		sendAck();
	}

	/**
	 * Implements the invite operation
	 *
	 * @param request the client request
	 */
	private void invite(JSONObject request) {
		// parsing request
		String username = (String) request.get(Fields.USERNAME);
		String creator  = (String) request.get(Fields.DOC_CREATOR);
		String docName  = (String) request.get(Fields.DOC_NAME);

		// get the document to share
		Document document;
		try {
			document = DocumentManager.getAsCreator(currentUser, DocumentManager.makeKey(creator, docName));
		} catch (UserNotAllowedException e) {
			sendError("You cannot share other users' documents");
			System.err.println(currentUser + " not allowed to share " + docName);
			return;
		} catch (InexistentDocumentException e) {
			sendError("Inexistent document: " + docName);
			System.err.println(docName + " inexistent");
			return;
		}

		// get the user to invite
		User user = Server.userManager.get(username);
		if (user == null) {
			sendError(username + " inexistent");
			return;
		} else if (user == currentUser) {
			sendError("You cannot invite yourself");
			return;
		}

		if (!document.shareWith(user)) {
			sendError(docName + " already shared with " + user.getUsername());
			System.err.println(docName + " already shared with " + user.getUsername());
			return;
		}

		synchronized (user.sharedDocuments) {
			if (!user.sharedDocuments.contains(document))
				user.sharedDocuments.add(document);
		}

		JSONObject notification = new JSONObject();
		notification.put(Fields.DOC_NAME, document.getName())
				.put(Fields.DOC_CREATOR, document.getCreator().getUsername())
				.put(Fields.SECTIONS, document.getNumberOfSections())
				.put(Fields.IS_SHARED, document.isShared());

		// because RMI calls are not asynchronous
		new Thread(() -> {
				try {
					user.sendNotification(notification.toString());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
		}).start();

		sendAck();
	}

	/**
	 * Implements the list operation
	 */
	private void list() {
		JSONObject reply = new JSONObject();
		JSONObject document;
		List<Document> myDocuments = currentUser.getMyDocuments();

		reply.put(Fields.STATUS, Fields.STATUS_OK);
		JSONArray docArray = new JSONArray();
		for (Document myDoc : myDocuments) {
			document = new JSONObject();
			document.put(Fields.DOC_NAME, myDoc.getName())
					.put(Fields.DOC_CREATOR, myDoc.getCreator().getUsername())
					.put(Fields.SECTIONS, myDoc.getNumberOfSections())
					.put(Fields.IS_SHARED, myDoc.isShared());
			docArray.put(document);
		}
		synchronized (currentUser.sharedDocuments) {
			for (Document sharedDoc : currentUser.sharedDocuments) {
				document = new JSONObject();
				document.put(Fields.DOC_NAME, sharedDoc.getName())
						.put(Fields.DOC_CREATOR, sharedDoc.getCreator().getUsername())
						.put(Fields.SECTIONS, sharedDoc.getNumberOfSections())
						.put(Fields.IS_SHARED, true);
				docArray.put(document);
			}
		}
		reply.put(Fields.DOCS, docArray);
		sendMessage(reply);
	}

	/**
	 * Implements the send of a chat message
	 *
	 * @param request the client request
	 */
	private void chatMessage(JSONObject request) {
		Section section = currentUser.getEditingSection();

		// user isn't editing any section
		if (section == null) {
			sendError("You have to edit a section before using the chat");
			return;
		}

		String message = (String) request.get(Fields.CHAT_MSG);
		if (section.getParent().sendChatMessage(message, currentUser.getUsername()))
			sendAck();
		else
			sendError("Cannot send message");
	}

	/**
	 * Gets a document for editing or showing it
	 *
	 * @param docName the document name
	 * @param creator the document creator
	 *
	 * @return the document, if the current user is allowed
	 *         null otherwise
	 */
	private Document getDocument(String docName, String creator) {
		Document document;
		try {
			document = DocumentManager.getAsGuest(currentUser, DocumentManager.makeKey(creator, docName));
		} catch (UserNotAllowedException e) {
			sendError("Permission denied for: " + docName);
			System.err.println(currentUser + " not allowed to get " + docName);
			return null;
		} catch (InexistentDocumentException e) {
			sendError("Inexistent document: " + docName);
			System.err.println(docName + " inexistent");
			return null;
		}
		return document;
	}
}
