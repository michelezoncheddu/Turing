package turing.server;

import org.json.JSONArray;
import turing.Fields;

import org.json.JSONObject;
import turing.server.exceptions.InexistentDocumentException;
import turing.server.exceptions.PreExistentDocumentException;
import turing.server.exceptions.UserNotAllowedException;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
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
			String reqString;
			try {
				reqString = reader.readLine();
			} catch (IOException e) { // communication error with the client
				logout();
				e.printStackTrace();
				break;
			}

			// client disconnected
			if (reqString == null) {
				logout();
				try {
					reader.close();
					writer.close();
					clientConnection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out.println("Client disconnected");
				break;
			}

			// TODO: validate messages format, check if currentUser is NOT NULL and currentUser is the same inside req
			JSONObject req = new JSONObject(reqString);
			try {
				handleOperation(req);
			} catch (IOException e) { // communication error with the client
				logout();
				e.printStackTrace();
			}
		}

		// terminating thread
		try {
			reader.close();
			writer.close();
			clientConnection.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.println("Thread " + Thread.currentThread() + " terminated");
		}
	}

	/**
	 * Sends a ack message to the client
	 *
	 * @throws IOException if a network error occurs
	 */
	private void sendAck() throws IOException {
		JSONObject message = new JSONObject();
		message.put(Fields.STATUS, Fields.STATUS_OK);
		writer.write(message.toString());
		writer.newLine();
		writer.flush();
	}

	/**
	 * Sends a error message to the client
	 *
	 * @param msg the explanation of the error to be sent
	 *
	 * @throws IOException if a network error occurs
	 */
	private void sendError(String msg) throws IOException {
		JSONObject message = new JSONObject();
		message.put(Fields.STATUS, Fields.STATUS_ERR)
				.put(Fields.ERR_MSG, msg);
		writer.write(message.toString());
		writer.newLine();
		writer.flush();
	}

	/**
	 * Switches the request to the handlers
	 *
	 * @param request the message to handle
	 *
	 * @throws IOException if a network error occurs
	 */
	private void handleOperation(JSONObject request) throws IOException {
		switch ((String) request.get(Fields.OPERATION)) {
		case Fields.OPERATION_LOGIN:
			login(request);
			break;

		case Fields.OPERATION_LOGOUT:
			logout();
			break;

		case Fields.OPERATION_CREATE_DOC:
			createDocument(request);
			break;

		case Fields.OPERATION_EDIT_SECTION:
			editSection(request);
			break;

		case Fields.OPERATION_END_EDIT:
			endEdit(request);
			break;

		case Fields.OPERATION_INVITE:
			invite(request);
			break;

		case Fields.OPERATION_LIST:
			list();
			break;

		case Fields.OPERATION_CHAT_MSG:
			sendMessage(request);
			break;

		default:
			System.err.println("Operation " + request.get(Fields.OPERATION) + " unknown");
		}
	}

	/**
	 * Implements the login operation
	 *
	 * @param request the client request
	 *
	 * @throws IOException if a network error occurs
	 */
	private void login(JSONObject request) throws IOException {
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
	 *
	 * @throws IOException if a network error occurs
	 */
	private void createDocument(JSONObject request) throws IOException {
		// parsing request
		String docName = (String) request.get(Fields.DOCUMENT_NAME);
		int sections = (Integer) request.get(Fields.NUMBER_OF_SECTIONS);

		// creating the new document
		Document newDoc;
		try {
			newDoc = new Document(docName, currentUser, sections);
		} catch (PreExistentDocumentException e) {
			sendError("Document already created");
			return;
		}
		currentUser.addDocument(newDoc);
		DocumentManager.put(newDoc);
		sendAck();
	}

	/**
	 * Implements the edit section operation
	 *
	 * @param request the client request
	 *
	 * @throws IOException if a network error occurs
	 */
	private void editSection(JSONObject request) throws IOException {
		if (currentUser.getEditingSection() != null) {
			sendError("You're already editing a section");
			return;
		}

		// parsing request
		String creator    = (String)  request.get(Fields.DOCUMENT_CREATOR);
		String docName    = (String)  request.get(Fields.DOCUMENT_NAME);
		int sectionNumber = (Integer) request.get(Fields.DOCUMENT_SECTION);

		Document document;
		try {
			document = DocumentManager.getAsGuest(currentUser, DocumentManager.makeKey(creator, docName));
		} catch (UserNotAllowedException e) {
			sendError("Permission denied for: " + docName);
			System.err.println(currentUser + " not allowed to modify " + docName);
			return;
		} catch (InexistentDocumentException e) {
			sendError("Inexistent document: " + docName);
			System.err.println(docName + " inexistent");
			return;
		}

		Section section = document.getSection(sectionNumber);
		if (section == null) {
			sendError("Inexistent section: " + sectionNumber);
			return;
		}

		// check if section is unlocked
		if (section.startEdit(currentUser)) {
			currentUser.setEditingSection(section); // lock user

			// send section content
			String content = section.getContent();
			JSONObject reply = new JSONObject();
			reply.put(Fields.STATUS, Fields.STATUS_OK)
					.put(Fields.SECTION_CONTENT, content)
					.put(Fields.CHAT_ADDRESS, document.getChatAddress().getHostAddress());
			writer.write(reply.toString());
			writer.newLine();
			writer.flush();
		} else {
			sendError("Another user is editing this section");
		}
	}

	/**
	 * Implements the end edit operation
	 *
	 * @param request the client request
	 *
	 * @throws IOException if a network error occurs
	 */
	private void endEdit(JSONObject request) throws IOException {
		Section section = currentUser.getEditingSection();

		// user isn't editing any section
		if (section == null) {
			sendError("You're not editing any section");
			return;
		}

		// new section content
		String content = request.has(Fields.SECTION_CONTENT) ? (String) request.get(Fields.SECTION_CONTENT) : null;

		section.endEdit(currentUser, content); // unlock section
		currentUser.setEditingSection(null);   // unlock user
		sendAck();
	}

	/**
	 * Implements the invite operation
	 *
	 * @param request the client request
	 */
	private void invite(JSONObject request) throws IOException {
		// parsing request
		String username = (String) request.get(Fields.USERNAME);
		String creator  = (String) request.get(Fields.DOCUMENT_CREATOR);
		String docName  = (String) request.get(Fields.DOCUMENT_NAME);

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

		if (document.isSharedWith(user)) {
			sendError(docName + " already shared with " + user.getUsername());
			System.err.println(docName + " already shared with " + user.getUsername());
			return;
		}

		document.shareWith(user);
		synchronized (user.sharedDocuments) {
			if (!user.sharedDocuments.contains(document))
				user.sharedDocuments.add(document);
		}

		JSONObject notification = new JSONObject();
		notification.put(Fields.DOCUMENT_NAME, document.getName())
				.put(Fields.DOCUMENT_CREATOR, document.getCreator().getUsername())
				.put(Fields.NUMBER_OF_SECTIONS, document.getNumberOfSections())
				.put(Fields.IS_SHARED, document.isShared());
		user.sendNotification(notification.toString());
		sendAck();
	}

	/**
	 * Implements the list operation
	 *
	 * @throws IOException if a network error occurs
	 */
	private void list() throws IOException {
		JSONObject message = new JSONObject();
		JSONObject document;
		List<Document> myDocuments = currentUser.getMyDocuments();

		message.put(Fields.STATUS, Fields.STATUS_OK);
		JSONArray docArray = new JSONArray();
		for (Document myDoc : myDocuments) {
			document = new JSONObject();
			document.put(Fields.DOCUMENT_NAME, myDoc.getName())
					.put(Fields.DOCUMENT_CREATOR, myDoc.getCreator().getUsername())
					.put(Fields.NUMBER_OF_SECTIONS, myDoc.getNumberOfSections())
					.put(Fields.IS_SHARED, myDoc.isShared());
			docArray.put(document);
		}
		synchronized (currentUser.sharedDocuments) {
			for (Document sharedDoc : currentUser.sharedDocuments) {
				document = new JSONObject();
				document.put(Fields.DOCUMENT_NAME, sharedDoc.getName())
						.put(Fields.DOCUMENT_CREATOR, sharedDoc.getCreator().getUsername())
						.put(Fields.NUMBER_OF_SECTIONS, sharedDoc.getNumberOfSections())
						.put(Fields.IS_SHARED, true);
				docArray.put(document);
			}
		}
		message.put(Fields.DOCUMENTS, docArray);
		writer.write(message.toString());
		writer.newLine();
		writer.flush();
	}

	/**
	 * Implements the send of a chat message
	 *
	 * @param request the client request
	 *
	 * @throws IOException if a network error occurs
	 */
	private void sendMessage(JSONObject request) throws IOException {
		Section section = currentUser.getEditingSection();

		// user isn't editing any section
		if (section == null) {
			sendError("You have to edit a section before using the chat");
			return;
		}

		section.getParent().sendMessage((String) request.get(Fields.CHAT_MSG), currentUser.getUsername());
		sendAck();
	}
}
