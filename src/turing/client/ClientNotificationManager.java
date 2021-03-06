package turing.client;

import org.json.JSONObject;
import turing.ClientNotificationManagerAPI;
import turing.Fields;

import javax.swing.*;
import java.rmi.RemoteException;

/**
 * Implements a client notification service
 */
public class ClientNotificationManager implements ClientNotificationManagerAPI {

	/**
	 * Creates a new client notification mananger
	 */
	public ClientNotificationManager() {
		super();
	}

	/**
	 * Sends a notification to the client by the server
	 *
	 * @param notification the notification to send
	 *
	 * @throws RemoteException if a RMI communication error occurs
	 */
	public void sendNotification(String notification) throws RemoteException {
		JSONObject document = new JSONObject(notification);

		// parsing notification
		String  name     = (String)  document.get(Fields.DOC_NAME);
		String  creator  = (String)  document.get(Fields.DOC_CREATOR);
		int     sections = (Integer) document.get(Fields.SECTIONS);
		boolean shared   = (Boolean) document.get(Fields.IS_SHARED);

		// add the new document
		Client.frame.addDocument(new Document(name, creator, sections, shared));
		SwingUtilities.invokeLater(() -> Client.frame.showInfoDialog(creator + " invited you to edit " + name));
	}
}
