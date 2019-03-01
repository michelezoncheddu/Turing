package turing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallbackHelloClientInterface extends Remote {

	/* Metodo invocato dal server per effettuare una callback a un client remoto. */
	public void notifyMe(String message) throws RemoteException;
}
