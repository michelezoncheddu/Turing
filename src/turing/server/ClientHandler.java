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
	private Socket clientSocket;
	private User currentUser = null;
	private BufferedWriter writer;

	/**
	 * TO DO
	 */
	public ClientHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
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

		try {
			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
			writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		while (true) {
			String reqString;
			try {
				reqString = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace(); // TODO: close streams, socket and disconnect user?
				break;
			}

			if (reqString == null) { // client disconnected
				if (currentUser != null)
					currentUser.setOnline(false);
				try {
					reader.close();
					writer.close();
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out.println(currentUser.getUsername() + " disconnected");
				break;
			}

			// HYPOTHESIS: messages are well formed
			JSONObject req = new JSONObject(reqString);
			try {
				handleOperation(req);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// terminating thread
		try {
			reader.close();
			writer.close();
			clientSocket.close();
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
		synchronized (writer) {
			message.write(writer);
			writer.newLine();
			writer.flush();
		}
	}

	/**
	 * TO DO
	 */
	private void handleOperation(JSONObject req) throws IOException {
		switch ((String) req.get(Fields.OPERATION)) {
		case Fields.OPERATION_LOGIN:
			boolean success = userManager.logIn((String) req.get(Fields.USERNAME), (String) req.get(Fields.PASSWORD));
			if (success) {
				currentUser = userManager.getUser((String) req.get(Fields.USERNAME));
				currentUser.writer = writer;

				List<Document> myDocs = currentUser.getMyDocuments();
				List<Document> sharedDocs = currentUser.getSharedDocuments();
				JSONObject ack = new JSONObject();
				ack.put(Fields.STATUS, Fields.STATUS_OK);
				ack.put(Fields.INCOMING, myDocs.size() + sharedDocs.size());

				synchronized (writer) {
					ack.write(writer);
					writer.newLine();
					for (Document myDoc : myDocs) {
						JSONObject doc = new JSONObject();
						doc.put("name", myDoc.getName());
						doc.put("creator", myDoc.getCreator().getUsername());
						doc.put("sections", myDoc.getSections().length);
						doc.put("shared", "no"); // not always
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

				out.println(Thread.currentThread() + " " + currentUser.getUsername() + " connected");
			} else {
				sendMessage(Fields.STATUS_ERR);
				out.println(Thread.currentThread() + " can't connect " + req.get(Fields.USERNAME));
			}
			break;

		case Fields.OPERATION_CREATE_DOC:
			String docName = (String) req.get(Fields.DOC_NAME);
			int sections = (Integer) req.get(Fields.SECTIONS);
			Document doc;
			// try {
				doc = new Document(docName, sections, currentUser);
			// } catch (MyException e) {
				// sendMessage(writer, "err");
				// return;
			//}
			currentUser.addMyDocument(doc);
			sendMessage(Fields.STATUS_OK);
			break;
		}
	}
}
