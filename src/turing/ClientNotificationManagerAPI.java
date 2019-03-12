package turing;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for a client notification service
 */
public interface ClientNotificationManagerAPI extends Remote {

	/**
	 * Sends a notification to the client by the server
	 *
	 * @param notification the notification to send
	 *
	 * @throws RemoteException if a RMI communication error occurs
	 */
	void sendNotification(String notification) throws RemoteException;
}
