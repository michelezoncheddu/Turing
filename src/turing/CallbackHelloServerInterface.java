package turing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallbackHelloServerInterface extends Remote {

	/* registrazione per il callback */
	public void registerForCallback(CallbackHelloClientInterface callbackClient) throws RemoteException;

	/* cancella registrazione per il callback */
	public void unregisterForCallback(CallbackHelloClientInterface callbackClient) throws RemoteException;
}
