package turing.server;

import turing.CallbackHelloClientInterface;
import turing.CallbackHelloServerInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class CallbackHelloServerImpl implements CallbackHelloServerInterface {

	private List<CallbackHelloClientInterface> clients;

	public CallbackHelloServerImpl() throws RemoteException {
		clients = new ArrayList<>();
	}

	public synchronized void registerForCallback(CallbackHelloClientInterface callbackClient) throws RemoteException {
		if (!clients.contains(callbackClient)) {
			clients.add(callbackClient);
			System.out.println("New client registered");
		}
	}

	/* annulla registrazione per il callback */
	public synchronized void unregisterForCallback(CallbackHelloClientInterface callbackClient) throws RemoteException {
		if (clients.remove(callbackClient))
			System.out.println("Client unregistered");
		else
			System.out.println("Unable to unregister client");
	}

	private synchronized void doCallbacks(String message) throws RemoteException {
		System.out.println("Starting callbacks.");
		for (CallbackHelloClientInterface client : clients) {
			client.notifyMe(message);
		}
		System.out.println("Callbacks complete.");
	}
}
