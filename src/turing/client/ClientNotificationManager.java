package turing.client;

import org.json.JSONObject;
import turing.ClientNotificationManagerAPI;
import turing.Fields;

import javax.swing.*;
import java.rmi.RemoteException;

public class ClientNotificationManager implements ClientNotificationManagerAPI {

	public void sendNotification(String notification) throws RemoteException {
		JSONObject document = new JSONObject(notification);
		String  name     = (String)  document.get(Fields.DOCUMENT_NAME);
		String  creator  = (String)  document.get(Fields.DOCUMENT_CREATOR);
		int     sections = (Integer) document.get(Fields.NUMBER_OF_SECTIONS);
		boolean shared   = (Boolean) document.get(Fields.IS_SHARED);
		Client.frame.addDocument(new Document(name, creator, sections, shared));
		JOptionPane.showMessageDialog(Client.frame, creator + " invited you to edit " + name);
	}
}
