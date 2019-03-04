package turing.server;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents user data
 */
public class User {
	private final String username;
	private String password;
	private boolean online;
	private Section editingSection;

	final List<Document> myDocuments;
	final List<Document> sharedDocuments;
	final ConcurrentLinkedQueue<JSONObject> pendingNotifications;

	/**
	 * Creates a new user
	 */
	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.myDocuments = new LinkedList<>();
		this.sharedDocuments = new LinkedList<>();
		this.pendingNotifications = new ConcurrentLinkedQueue<>();
		this.online = false;
		this.editingSection = null;
	}

	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public boolean isOnline() {
		return online;
	}
	public Section getEditingSection() {
		return editingSection;
	}

	/**
	 * TO DO
	 */
	public synchronized boolean setOnline(boolean newStatus) {
		if (online == newStatus)
			return false;
		online = newStatus;
		return true;
	}

	/**
	 * TO DO
	 */
	public void setEditingSection(Section section) {
		editingSection = section;
	}
}
