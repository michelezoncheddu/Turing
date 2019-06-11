package turing.server;

import turing.ClientNotificationManagerAPI;
import turing.ServerNotificationManagerAPI;
import turing.server.exceptions.InexistentUserException;

import java.rmi.RemoteException;

/**
 * Implements a server notification service
 */
public class ServerNotificationManager implements ServerNotificationManagerAPI {

	/**
	 * Creates a new server notification mananger
	 */
	public ServerNotificationManager() {
		super();
	}

	/**
	 * Registers an user for receiving server notifications
	 *
	 * @param username the user username
	 * @param password the user password
	 * @param notifier the client notifier
	 *
	 * @throws NullPointerException if at least a parameter is null
	 * @throws RemoteException      if a RMI communication error occurs
	 */
	public void registerForNotifications(String username, String password, ClientNotificationManagerAPI notifier)
			throws NullPointerException, RemoteException {
		if (username == null || password == null || notifier == null)
			throw new NullPointerException("Username, password and notifier must not be null");

		User user;
		try {
			user = Server.userManager.get(username);
		} catch (InexistentUserException e) {
			return;
		}

		if (user.isOnline() && user.getPassword().equals(password)) {
			user.setNotifier(notifier);
			user.flushPendingNotifications();
		}
	}
}
