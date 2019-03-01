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

public class ClientHandler implements Runnable {
	private static DocumentManager documentManager;
	private static UserManager userManager;
	private Socket clientConnection;
	private User currentUser = null;
	private BufferedWriter writer;

	/**
	 * TO DO
	 */
	public ClientHandler(Socket clientConnection) {
		this.clientConnection = clientConnection;
	}

	/**
	 * TO DO
	 */
	public static void setManagers(DocumentManager dm, UserManager um) {
		documentManager = dm;
		userManager = um;
	}

	/**
	 * TO DO
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
			// TODO: release locks
			if (reqString == null) {
				if (currentUser != null) {
					currentUser.setOnline(false);
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
	 * TO DO
	 */
	private void sendStatusMessage(String status) throws IOException {
		JSONObject message = new JSONObject();
		message.put(Fields.STATUS, status);
		message.write(writer);
		writer.newLine();
		writer.flush();
	}

	/**
	 * TO DO
	 */
	private void handleOperation(JSONObject req) throws IOException {
		switch ((String) req.get(Fields.OPERATION)) {
		case Fields.OPERATION_LOGIN:
			handleLogin(req);
			break;

		case Fields.OPERATION_CREATE_DOC:
			handleCreateDoc(req);
			break;

		case Fields.OPERATION_LIST:
			handleList();
			break;

		case Fields.OPERATION_EDIT_SECTION:
			handleEditSection(req);
			break;

		default:
			System.err.println("Operation " + req.get(Fields.OPERATION) + " unknown");
		}
	}

	/**
	 * TO DO
	 */
	private void handleLogin(JSONObject req) throws IOException {
		String username = (String) req.get(Fields.USERNAME);
		String password = (String) req.get(Fields.PASSWORD);
		// try to log user
		boolean success = userManager.logIn(username, password);
		if (success) {
			currentUser = userManager.getUser(username);

			sendStatusMessage(Fields.STATUS_OK);
			out.println(Thread.currentThread() + " " + currentUser.getUsername() + " connected");
		} else {
			sendStatusMessage(Fields.STATUS_ERR);
			out.println(Thread.currentThread() + " can't connect " + username);
		}
	}

	/**
	 * TO DO
	 */
	private void handleCreateDoc(JSONObject req) throws IOException {
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
		documentManager.add(newDoc);
		sendStatusMessage(Fields.STATUS_OK);
	}

	/**
	 * TO DO
	 */
	private void handleList() throws IOException {
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
				doc.put("shared", "no");
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
	 * TO DO
	 */
	private void handleEditSection(JSONObject req) throws IOException {
		String creator = (String) req.get(Fields.DOCUMENT_CREATOR);
		String docName = (String) req.get(Fields.DOCUMENT_NAME);
		int sectionNumber = (Integer) req.get(Fields.DOCUMENT_SECTION);

		Document document;
		try {
			document = documentManager.get(currentUser, creator, docName);
		} catch (UserNotAllowedException e) { // TODO: send error to client
			System.err.println(currentUser + " not allowed to modify " + docName);
			return;
		} catch (InexistentDocumentException e) { // TODO: send error to client
			System.err.println(docName + " inexistent");
			return;
		}

		Section section = document.getSection(sectionNumber);
		if (section == null)
			return; // TODO: send error to client

		if (section.startEdit(currentUser)) {
			// send section to client
			String data = section.getContent();
			// send data
		} else {
			// send error
		}
	}
}
