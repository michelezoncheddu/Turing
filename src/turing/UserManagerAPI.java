package turing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserManagerAPI extends Remote {

	boolean signUp(String username, String password) throws RemoteException;
}
