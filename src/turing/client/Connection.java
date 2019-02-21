package turing.client;

import turing.Fields;
import turing.UserManagerAPI;

import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Connection {
	public BufferedWriter writer;
	public BufferedReader reader, backgroundReader;

	/**
	 * Initializes the connection with the server
	 */
	public Connection(InetSocketAddress defaultAddress, InetSocketAddress backgroundAddress) throws IOException {
		Socket defaultConnection = new Socket();
		Socket backgroundConnection = new Socket();
		defaultConnection.connect(defaultAddress);
		backgroundConnection.connect(backgroundAddress);
		writer = new BufferedWriter(new OutputStreamWriter(defaultConnection.getOutputStream(), StandardCharsets.UTF_8));
		reader = new BufferedReader(new InputStreamReader(defaultConnection.getInputStream(), StandardCharsets.UTF_8));
		backgroundReader = new BufferedReader(new InputStreamReader(backgroundConnection.getInputStream(), StandardCharsets.UTF_8));
		new Thread(new BackgroundListener(backgroundReader)).start();
	}

	/**
	 * TO DO
	 */
	public void signUp(String username, String password) {
		if (username.isBlank() || password.isBlank())
			return;

		UserManagerAPI serverObject;
		Remote remoteObject;
		try {
			Registry r = LocateRegistry.getRegistry();
			remoteObject = r.lookup(Client.SERVER_NAME);
			serverObject = (UserManagerAPI) remoteObject;
			boolean success = serverObject.signUp(username, password);
			if (success)
				JOptionPane.showMessageDialog(Client.frame, username + " registered");
			else
				JOptionPane.showMessageDialog(Client.frame, "Can't register " + username,
						"Error", JOptionPane.ERROR_MESSAGE); // TODO: specify error
		} catch (RemoteException e) {
			Client.frame.showErrorDialog("Communication error", e);
		} catch (NotBoundException e) {
			Client.frame.showErrorDialog("Unable to find registration service", e);
		}
	}

	/**
	 * TO DO
	 */
	public void logIn(String username, String password) {
		if (username.isBlank() || password.isBlank())
			return;

		// create login request
		JSONObject json = new JSONObject();
		json.put(Fields.OPERATION, Fields.OPERATION_LOGIN);
		json.put(Fields.USERNAME, username);
		json.put(Fields.PASSWORD, password);
		json.write(writer);

		// send login request and wait reply
		String jsonString;
		try {
			writer.newLine();
			writer.flush();
			jsonString = reader.readLine();
		} catch (IOException e) {
			Client.frame.showErrorDialog("Communication error", e);
			return;
		}

		JSONObject reply = new JSONObject(jsonString);
		if (reply.get(Fields.STATUS).equals(Fields.STATUS_OK)) { // logged successfully
			Client.frame.createWorkspace();

			if (reply.has(Fields.INCOMING_MESSAGES)) {
				int incoming = (Integer) reply.get(Fields.INCOMING_MESSAGES);
				// download table data
				for (int i = 0; i < incoming; i++) {
					try {
						jsonString = reader.readLine();
					} catch (IOException e) {
						Client.frame.showErrorDialog("Communication error", e);
					}
					JSONObject jsonDoc = new JSONObject(jsonString);
					Document doc = new Document((String) jsonDoc.get("name"),
							(String) jsonDoc.get("creator"), (Integer) jsonDoc.get("sections"));
					Client.frame.addDocument(doc);
				}
			}
		}
	}

	/**
	 * TO DO
	 */
	public void createDocument(String documentName, int sections) {
		JSONObject req = new JSONObject();
		req.put(Fields.OPERATION, Fields.OPERATION_CREATE_DOC);
		req.put(Fields.DOCUMENT_NAME, documentName);
		req.put(Fields.NUMBER_OF_SECTIONS, sections);
		req.write(writer);

		// send message
		try {
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			Client.frame.showErrorDialog("Communication error", e);
			return;
		}

		// wait reply
		String str;
		try {
			str = reader.readLine();
		} catch (IOException e) {
			Client.frame.showErrorDialog("Communication error", e);
			return;
		}

		JSONObject reply = new JSONObject(str);
		if (reply.get(Fields.STATUS).equals(Fields.STATUS_OK))
			JOptionPane.showMessageDialog(Client.frame, "Document created");
		else
			JOptionPane.showMessageDialog(Client.frame, "Error creating document");
	}

	/**
	 * TO DO
	 */
	public void editSection(int index) {
		if (index < 0) {
			JOptionPane.showMessageDialog(Client.frame, "Select a section");
			return;
		}
		Document doc = Client.frame.getLastSelectedDocument();
		JSONObject req = new JSONObject();
		req.put(Fields.OPERATION, Fields.OPERATION_EDIT_SECTION);
		req.put(Fields.DOCUMENT_NAME, doc.getName());
		req.put(Fields.DOCUMENT_CREATOR, doc.getCreator());
		req.put(Fields.DOCUMENT_SECTION, index + 1);

		req.write(writer);
		try {
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			Client.frame.showErrorDialog("Can't edit document", e);
		}
		JOptionPane.showMessageDialog(Client.frame, index);
	}
}