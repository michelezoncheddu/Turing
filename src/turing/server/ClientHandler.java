package turing.server;

import turing.Fields;

import org.json.JSONObject;
import turing.server.exceptions.InexistentDocumentException;
import turing.server.exceptions.PreExistentDocumentException;
import turing.server.exceptions.UserNotAllowedException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static java.lang.System.out;

/**
 * A thread that implements the operations of a individual client
 */
public class ClientHandler implements Runnable {
	private Socket clientConnection;
	private User currentUser = null;
	private BufferedWriter writer;

	/**
	 * Creates a new client handler with a connection with a client
	 */
	public ClientHandler(Socket clientConnection) {
		this.clientConnection = clientConnection;
	}

	/**
	 * Client handling loop
	 */
	@Override
	public void run() {
		BufferedReader reader;

		// open streams
		try {
			reader = new BufferedReader(new InputStreamReader(clientConnection.getInputStream(), StandardCharsets.UTF_8));
			writer = new BufferedWriter(new OutputStreamWriter(clientConnection.getOutputStream(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		while (true) {
			// read request
			String reqString;
			try {
				reqString = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace(); // TODO: close streams, defaultConnection and disconnect user?
				break;
			}

			// client disconnected
			if (reqString == null) {
				if (currentUser != null) {
					currentUser.setOnline(false);
					Section currentSection = currentUser.getEditingSection();
					if (currentSection != null)
						currentSection.endEdit(currentUser);
					currentUser.setEditingSection(null);
					currentUser = null; // ready for another client
				}
				try {
					reader.close();
					writer.close();
					clientConnection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out.println(Thread.currentThread() + ": client disconnected");
				break;
			}

			// TODO: validate messages structure, check if user is online and currentUser is the same inside req
			JSONObject req = new JSONObject(reqString);
			try {
				handleOperation(req);
			} catch (IOException e) {
				e.printStackTrace(); // TODO: specify problem and send error to client
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
	 * Sends a status message to the client
	 */
	private void sendStatusMessage(String status) throws IOException {
		JSONObject message = new JSONObject();
		message.put(Fields.STATUS, status);
		message.write(writer);
		writer.newLine();
		writer.flush();
	}

	/**
	 * Sorts the request to the handlers
	 */
	private void handleOperation(JSONObject req) throws IOException {
		switch ((String) req.get(Fields.OPERATION)) {
		case Fields.OPERATION_LOGIN:
			login(req);
			break;

		case Fields.OPERATION_CREATE_DOC:
			createDoc(req);
			break;

		case Fields.OPERATION_LIST:
			list();
			break;

		case Fields.OPERATION_EDIT_SECTION:
			editSection(req);
			break;

		default:
			System.err.println("Operation " + req.get(Fields.OPERATION) + " unknown");
		}
	}

	/**
	 * Implements the login operation
	 */
	private void login(JSONObject req) throws IOException {
		String username = (String) req.get(Fields.USERNAME);
		String password = (String) req.get(Fields.PASSWORD);

		// try to log user
		boolean success = Server.userManager.logIn(username, password);
		if (success) {
			currentUser = Server.userManager.getUser(username);
			sendStatusMessage(Fields.STATUS_OK);
			out.println(Thread.currentThread() + " " + currentUser.getUsername() + " connected");
		} else {
			sendStatusMessage(Fields.STATUS_ERR);
			out.println(Thread.currentThread() + " can't connect " + username);
		}
	}

	/**
	 * Implements the create document operation
	 */
	private void createDoc(JSONObject req) throws IOException {
		String docName = (String) req.get(Fields.DOCUMENT_NAME);
		int sections = (Integer) req.get(Fields.NUMBER_OF_SECTIONS);
		Document newDoc;
		try {
			newDoc = new Document(docName, currentUser, sections);
		} catch (PreExistentDocumentException e) {
			sendStatusMessage(Fields.STATUS_ERR);
			return;
		}
		currentUser.myDocuments.add(newDoc);
		Server.documentManager.add(newDoc);
		sendStatusMessage(Fields.STATUS_OK);
	}

	/**
	 * Implements the list operation
	 */
	private void list() throws IOException {
		JSONObject msg = new JSONObject();

		synchronized (currentUser.sharedDocuments) {
			msg.put(Fields.INCOMING_MESSAGES, currentUser.myDocuments.size() + currentUser.sharedDocuments.size());
			msg.write(writer);
			writer.newLine();
			for (Document myDoc : currentUser.myDocuments) {
				JSONObject doc = new JSONObject();
				doc.put("name", myDoc.getName());
				doc.put("creator", myDoc.getCreator().getUsername());
				doc.put("sections", myDoc.getNumberOfSections());
				doc.put("shared", "no"); // TODO: maybe yes
				doc.write(writer);
				writer.newLine();
			}
			for (Document sharedDoc : currentUser.sharedDocuments) {
				JSONObject doc = new JSONObject();
				doc.put("name", sharedDoc.getName());
				doc.put("creator", sharedDoc.getCreator().getUsername());
				doc.put("sections", sharedDoc.getNumberOfSections());
				doc.put("shared", "yes");
				doc.write(writer);
				writer.newLine();
			}
		}
		writer.flush();
	}

	/**
	 * Implements the edit section operation
	 */
	private void editSection(JSONObject req) throws IOException {
		if (currentUser.getEditingSection() != null) {
			// TODO: send error
			return;
		}

		String creator = (String) req.get(Fields.DOCUMENT_CREATOR);
		String docName = (String) req.get(Fields.DOCUMENT_NAME);
		int sectionNumber = (Integer) req.get(Fields.DOCUMENT_SECTION);

		Document document;
		try {
			document = Server.documentManager.get(currentUser, creator, docName);
		} catch (UserNotAllowedException e) { // TODO: send error to client
			System.err.println(currentUser + " not allowed to modify " + docName);
			return;
		} catch (InexistentDocumentException e) { // TODO: send error to client
			System.err.println(docName + " inexistent");
			return;
		}

		Section section = document.getSection(sectionNumber);
		if (section == null)
			return; // TODO: send error to client, inexistent section

		if (section.startEdit(currentUser)) {
			currentUser.setEditingSection(section);
			JSONObject reply = new JSONObject();
			String sectionContent = section.getContent();
			reply.put(Fields.SECTION_CONTENT, sectionContent);
			reply.write(writer);
			writer.newLine();
			writer.flush();
		} else {
			// TODO: send error
		}
	}
}
