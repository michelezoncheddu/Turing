package turing.server;

import turing.ClientNotificationManagerAPI;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents user data
 */
public class User {
	private final String username;
	private String password;
	private boolean onlineStatus;

	private final List<Document> myDocuments;
	private Section editingSection;

	private ClientNotificationManagerAPI notifier;
	private final ConcurrentLinkedQueue<String> pendingNotifications;

	final List<Document> sharedDocuments;

	/**
	 * Creates a new user
	 *
	 * @param username the user username
	 * @param password the user password
	 */
	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.onlineStatus = false;

		this.myDocuments = new LinkedList<>();
		this.editingSection = null;

		this.notifier = null;
		this.pendingNotifications = new ConcurrentLinkedQueue<>();

		this.sharedDocuments = new LinkedList<>();
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
	 * Returns the online status
	 *
	 * @return the online status
	 */
	public boolean isOnline() {
		return onlineStatus;
	}

	/**
	 * Returns the collection of user's documents
	 *
	 * @return the user's documents list
	 */
	public List<Document> getMyDocuments() {
		return myDocuments;
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
	 * @return true if has been possibile to change the status
	 *         false otherwise
	 */
	public synchronized boolean setOnline(boolean newStatus) {
		if (onlineStatus == newStatus)
			return false;
		onlineStatus = newStatus;
		return true;
	}

	/**
	 * Adds a document to the user's private document list
	 *
	 * @param document the document to add
	 */
	public void addDocument(Document document) {
		myDocuments.add(document);
	}

	/**
	 * Sets the currently editing section by the user
	 *
	 * @param section the section that is currently editing
	 */
	public void setEditingSection(Section section) {
		editingSection = section;
	}

	public void setNotifier(ClientNotificationManagerAPI notifier) {
		synchronized (this) { // to change notifier safely
			this.notifier = notifier;
		}
	}

	/**
	 * Sends a notification to the client, if it's online, otherwise, adds the notification in the pending queue
	 *
	 * @param notification the notification to send
	 *
	 * @throws RemoteException if a RMI communication error occurs
	 */
	public void sendNotification(String notification) throws RemoteException {
		synchronized (this) { // because notifier cannot change while sending notification
			if (notifier != null)
				notifier.sendNotification(notification);
			else
				pendingNotifications.add(notification);
		}
	}

	/**
	 * Sends all pending notifications to the client
	 *
	 * @throws RemoteException if a RMI communication error occurs
	 */
	public void flushPendingNotifications() throws RemoteException {
		synchronized (this) { // because notifier cannot change while sending notification
			if (notifier == null)
				return;

			while (!pendingNotifications.isEmpty())
				notifier.sendNotification(pendingNotifications.poll());
		}
	}
}
