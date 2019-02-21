package turing.server;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Contains user information.
 */
public class User {
	private String username;
	private String password;
	private List<Document> myDocuments;
	private List<Document> sharedDocuments;
	private Queue<JSONObject> pendingNotifications;
	private boolean online;
	public BufferedWriter backgroundWriter; // to send notifications

	/**
	 * Creates a new user.
	 */
	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.myDocuments = new LinkedList<>();
		this.sharedDocuments = new LinkedList<>();
		this.pendingNotifications = new ConcurrentLinkedQueue<>();
		this.online = false;
		this.backgroundWriter = null;
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
