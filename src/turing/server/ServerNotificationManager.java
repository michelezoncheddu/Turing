package turing.server;

import turing.ClientNotificationManagerAPI;
import turing.ServerNotificationManagerAPI;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ServerNotificationManager implements ServerNotificationManagerAPI {
	private List<ClientNotificationManagerAPI> clients = new ArrayList<>();

	public synchronized void registerForCallback(ClientNotificationManagerAPI callbackClient) throws RemoteException {
		if (!clients.contains(callbackClient)) {
			clients.add(callbackClient);
			System.out.println("New client registered");
		}
	}

	public synchronized void unregisterForCallback(ClientNotificationManagerAPI callbackClient) throws RemoteException {
		if (clients.remove(callbackClient))
			System.out.println("Client unregistered");
		else
			System.out.println("Unable to unregister client");
	}

	public synchronized void notifyAll(String message) throws RemoteException {
		for (ClientNotificationManagerAPI client : clients)
			client.notify(message);
	}
}
