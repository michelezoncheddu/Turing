package turing.client;

import org.json.JSONObject;
import turing.ClientNotificationManagerAPI;
import turing.Fields;

import java.rmi.RemoteException;

/**
 * Implements a client notification service
 */
public class ClientNotificationManager implements ClientNotificationManagerAPI {

	/**
	 * Sends a notification to the client by the server
	 *
	 * @param notification the notification to send
	 *
	 * @throws RemoteException if a RMI communication error occurs
	 */
	public void sendNotification(String notification) throws RemoteException {
		JSONObject document = new JSONObject(notification);
		String  name     = (String)  document.get(Fields.DOC_NAME);
		String  creator  = (String)  document.get(Fields.DOC_CREATOR);
		int     sections = (Integer) document.get(Fields.SECTIONS);
		boolean shared   = (Boolean) document.get(Fields.IS_SHARED);
		Client.frame.addDocument(new Document(name, creator, sections, shared));
		Client.frame.showInfoDialog(creator + " invited you to edit " + name);
	}
}
