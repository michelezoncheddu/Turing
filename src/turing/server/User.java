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
	 *
	 * @param username the user username
	 * @param password the user password
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

	/**
	 * Returns the user username
	 *
	 * @return the user username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Returns the user password
	 *
	 * @return the user password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns the user online status
	 *
	 * @return the user online status
	 */
	public boolean isOnline() {
		return online;
	}

	/**
	 * Return the section currently editing by the user
	 *
	 * @return the section that is currently editing
	 */
	public Section getEditingSection() {
		return editingSection;
	}

	/**
	 * Sets the online status
	 *
	 * @param newStatus the new online status
	 *
	 * @return true if has been possibile to change the online status
	 *         false otherwise
	 */
	public synchronized boolean setOnline(boolean newStatus) {
		if (online == newStatus)
			return false;
		online = newStatus;
		return true;
	}

	/**
	 * Sets the currently editing section by the user
	 *
	 * @param section the section that is currently editing
	 */
	public void setEditingSection(Section section) {
		editingSection = section;
	}
}
