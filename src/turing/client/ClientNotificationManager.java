package turing.client;

import turing.ClientNotificationManagerAPI;

import java.rmi.RemoteException;

public class ClientNotificationManager implements ClientNotificationManagerAPI {

	public void notify(String message) throws RemoteException {
		System.out.println(message);
	}
}
