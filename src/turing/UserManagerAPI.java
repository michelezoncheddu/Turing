package turing;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for an user manager
 */
public interface UserManagerAPI extends Remote {

	/**
	 * Registers a new user
	 *
	 * @param username the user username
	 * @param password the user password
	 *
	 * @return true if has been possibile to register the user
	 *         false otherwise
	 *
	 * @throws RemoteException if a RMI communication error occurs
	 */
	boolean signUp(String username, String password) throws RemoteException;
}
