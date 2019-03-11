package turing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientNotificationManagerAPI extends Remote {

	void sendNotification(String notification) throws RemoteException;
}
