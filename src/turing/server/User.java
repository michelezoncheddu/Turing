package turing.server;

import java.io.BufferedWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains user information.
 */
public class User {
	private String username;
	private String password;
	private List<Document> myDocuments;
	private List<Document> sharedDocuments;
	// Pending notifications
	private boolean online;
	public BufferedWriter writer;

	/**
	 * Creates a new user.
	 */
	public User(String username, String password) {
		this.username = username;
		this.password = password;
		myDocuments = new LinkedList<>();
		sharedDocuments = new LinkedList<>();
		this.online = false;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public synchronized boolean setOnline(boolean newStatus) {
		if (online == newStatus)
			return false;
		online = newStatus;
		return true;
	}

	public boolean isOnline() {
		return online;
	}

	public void addMyDocument(Document document) {
		myDocuments.add(document);
	}

	public void addSharedDocument(Document document) {
		sharedDocuments.add(document);
	}

	public List<Document> getMyDocuments() { return myDocuments; }

	public List<Document> getSharedDocuments() { return sharedDocuments; }
}
