package turing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerNotificationManagerAPI extends Remote {

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
	void registerForNotifications(
			String username, String password, ClientNotificationManagerAPI notifier)
			throws NullPointerException, RemoteException;
}
