package turing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientNotificationManagerAPI extends Remote {

	/* Metodo invocato dal server per effettuare una callback a un client remoto. */
	public void notify(String message) throws RemoteException;
}
