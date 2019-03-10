package turing.client;

import turing.Fields;

import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Implements the connection and the communication with the server
 */
public class Connection {

	// streams with the server
	private BufferedWriter writer;
	private BufferedReader reader;

	/**
	 * Initializes the connection with the server
	 *
	 * @param address the server address
	 *
	 * @throws IOException if a network error occurs
	 */
	public Connection(InetSocketAddress address) throws IOException {
		Socket defaultConnection = new Socket();
		defaultConnection.connect(address);
		writer = new BufferedWriter(new OutputStreamWriter(defaultConnection.getOutputStream(), StandardCharsets.UTF_8));
		reader = new BufferedReader(new InputStreamReader(defaultConnection.getInputStream(), StandardCharsets.UTF_8));
	}

	/**
	 * Executes the request/reply protocol
	 *
	 * @param request the message to send
	 * @return the reply message
	 */
	public JSONObject requestReply(JSONObject request) {
		String replyString;

		// send request and wait reply
		try {
			writer.write(request.toString());
			writer.newLine();
			writer.flush();
			replyString = reader.readLine();
		} catch (IOException e) {
			Client.frame.showErrorDialog("Communication error");
			return null;
		}
		return new JSONObject(replyString);
	}

	/**
	 * Downloads the documents table data
	 *
	 * @param incomingMessages number of messages to listen for
	 */
	public void downloadTablesData(Integer incomingMessages) {
		String messageString;
		JSONObject jsonDoc;
		for (int i = 0; i < incomingMessages; i++) {
			try {
				messageString = reader.readLine();
			} catch (IOException e) {
				Client.frame.showErrorDialog("Communication error");
				return;
			}
			jsonDoc = new JSONObject(messageString);
			Client.frame.addDocument(new Document(
					(String) jsonDoc.get(Fields.DOCUMENT_NAME),
					(String) jsonDoc.get(Fields.DOCUMENT_CREATOR),
					(Integer) jsonDoc.get(Fields.NUMBER_OF_SECTIONS),
					(Boolean) jsonDoc.get(Fields.IS_SHARED)));
		}
	}
}
