package turing.server;

import turing.ClientNotificationManagerAPI;
import turing.ServerNotificationManagerAPI;

import java.rmi.RemoteException;

public class ServerNotificationManager implements ServerNotificationManagerAPI {

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
			throw new NullPointerException();

		User user = Server.userManager.get(username);

		if (user != null && user.isOnline() && user.getPassword().equals(password)) {
			user.setNotifier(notifier);
			user.flushPendingNotifications();
		}
	}
}
