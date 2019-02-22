package turing.server;

import turing.Fields;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.lang.System.out;

public class ClientHandler implements Runnable {
	private static UserManager userManager;
	private Socket clientConnection, backgroundConnection;
	private User currentUser = null;
	private BufferedWriter writer, backgroundWriter;

	/**
	 * TO DO
	 */
	public ClientHandler(Socket clientConnection, Socket backgroundConnection) {
		this.clientConnection = clientConnection;
		this.backgroundConnection = backgroundConnection;
	}

	/**
	 * TO DO
	 */
	public static void setUserManager(UserManager um) {
		if (um != null)
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
			backgroundWriter = new BufferedWriter(new OutputStreamWriter(backgroundConnection.getOutputStream(), StandardCharsets.UTF_8));
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
					currentUser.backgroundWriter = null;
				}
				try {
					reader.close();
					writer.close();
					clientConnection.close();
					backgroundWriter.close();
					backgroundConnection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out.println(Thread.currentThread() + ": client disconnected");
				break;
			}

			// TODO: validate messages structure
			JSONObject req = new JSONObject(reqString);
			try {
				handleOperation(req);
			} catch (IOException e) {
				e.printStackTrace(); // TODO: specify problem
			}
		}

		// terminating thread
		try {
			reader.close();
			writer.close();
			clientConnection.close();
			backgroundWriter.close();
			backgroundConnection.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.println("Thread " + Thread.currentThread() + " terminated");
		}
	}

	/**
	 * TO DO
	 */
	private void sendMessage(String status) throws IOException {
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

		default:
			System.err.println("Operation " + req.get(Fields.OPERATION) + " unknown");
		}
	}

	/**
	 * TO DO
	 */
	private void handleLogin(JSONObject req) throws IOException {
		boolean success = userManager.logIn((String) req.get(Fields.USERNAME), (String) req.get(Fields.PASSWORD));
		if (success) {
			currentUser = userManager.getUser((String) req.get(Fields.USERNAME));
			currentUser.backgroundWriter = backgroundWriter;

			JSONObject ack = new JSONObject();
			ack.put(Fields.STATUS, Fields.STATUS_OK);
			ack.put(Fields.INCOMING_MESSAGES, currentUser.getMyDocuments().size() + currentUser.getSharedDocuments().size());

			ack.write(writer);
			writer.newLine();
			// send documents info
			handleList();
			out.println(Thread.currentThread() + " " + currentUser.getUsername() + " connected");
		} else {
			sendMessage(Fields.STATUS_ERR);
			out.println(Thread.currentThread() + " can't connect " + req.get(Fields.USERNAME));
		}
	}

	/**
	 * TO DO
	 */
	private void handleCreateDoc(JSONObject req) throws IOException {
		String docName = (String) req.get(Fields.DOCUMENT_NAME);
		int sections = (Integer) req.get(Fields.NUMBER_OF_SECTIONS);
		Document newDoc;
		// try {
			newDoc = new Document(docName, currentUser, sections);
		// } catch (SomeDocumentException e) { // TODO
		// sendMessage(writer, "err");
		// return;
		//}
		currentUser.addMyDocument(newDoc);
		sendMessage(Fields.STATUS_OK);
	}

	/**
	 * TO DO
	 */
	private void handleList() throws IOException {
		List<Document> myDocs = currentUser.getMyDocuments();
		List<Document> sharedDocs = currentUser.getSharedDocuments();
		// send incoming messages
		for (Document myDoc : myDocs) {
			JSONObject doc = new JSONObject();
			doc.put("name", myDoc.getName());
			doc.put("creator", myDoc.getCreator().getUsername());
			doc.put("sections", myDoc.getSections().length);
			doc.put("shared", "no");
			doc.write(writer);
			writer.newLine();
		}
		for (Document sharedDoc : sharedDocs) {
			JSONObject doc = new JSONObject();
			doc.put("name", sharedDoc.getName());
			doc.put("creator", sharedDoc.getCreator().getUsername());
			doc.put("sections", sharedDoc.getSections().length);
			doc.put("shared", "yes");
			doc.write(writer);
			writer.newLine();
		}
		writer.flush();
	}
}
