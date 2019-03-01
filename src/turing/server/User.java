package turing.server;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Contains user information.
 */
public class User {
	private String username;
	private String password;
	private boolean online;

	final List<Document> myDocuments;
	final List<Document> sharedDocuments;
	final ConcurrentLinkedQueue<JSONObject> pendingNotifications;

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

	@Override
	public String toString() {
		return username + ", online: " + online;
	}
}
