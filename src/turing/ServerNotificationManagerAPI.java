package turing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerNotificationManagerAPI extends Remote {

	/* registrazione per il callback */
	public void registerForCallback(ClientNotificationManagerAPI callbackClient) throws RemoteException;

	/* cancella registrazione per il callback */
	public void unregisterForCallback(ClientNotificationManagerAPI callbackClient) throws RemoteException;
}
