package turing.server;

import turing.ClientNotificationManagerAPI;
import turing.server.exceptions.AlreadyLoggedException;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Represents user data
 */
public class User {
	private final String username;
	private String password;
	private boolean onlineStatus = false;

	private final List<Document> myDocuments = new LinkedList<>();
	private Section editingSection = null;

	private ClientNotificationManagerAPI notifier = null;
	private final Queue<String> pendingNotifications = new LinkedList<>();

	final List<Document> sharedDocuments = new LinkedList<>();

	/**
	 * Creates a new user
	 *
	 * @param username the user username
	 * @param password the user password
	 */
	public User(String username, String password) {
		this.username = username;
		this.password = password;
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
	 * @throws AlreadyLoggedException if the user was already logged
	 */
	public synchronized void setOnline(boolean newStatus) throws AlreadyLoggedException {
		if (onlineStatus && newStatus)
			throw new AlreadyLoggedException(username + " already online");

		onlineStatus = newStatus;
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

	/**
	 * Sets the client notifier
	 *
	 * @param notifier the client notifier
	 */
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
		synchronized (this) { // because notifier must not change while sending notification
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
		synchronized (this) { // because notifier must not change while sending notification
			if (notifier == null)
				return;

			while (!pendingNotifications.isEmpty()) {
				notifier.sendNotification(pendingNotifications.poll());
			}
		}
	}
}
