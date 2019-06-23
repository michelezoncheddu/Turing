package turing.server;

import turing.UserManagerAPI;
import turing.server.exceptions.InexistentUserException;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a concurrent user manager
 */
public class UserManager extends RemoteServer implements UserManagerAPI {
	/**
	 * Key: username
	 */
	private Map<String, User> users = new ConcurrentHashMap<>();

	/**
	 * Creates a new user manager
	 */
	public UserManager() {
		super();
	}

	/**
	 * Registers a new user
	 *
	 * @param username the user username
	 * @param password the user password
	 *
	 * @return true if has been possibile to register the user (username not registered yet)
	 *         false otherwise
	 *
	 * @throws NullPointerException     if username is null or password is null
	 * @throws IllegalArgumentException if username contains the file separator character
	 * @throws RemoteException          if a RMI communication error occurs
	 */
	public boolean signUp(String username, String password)
			throws NullPointerException, IllegalArgumentException, RemoteException {
		if (username == null || password == null)
			throw new NullPointerException("Username and password must not be null");

		if (username.contains(File.separator))
			throw new IllegalArgumentException("The username can't contain this character: " + File.separator);

		return users.putIfAbsent(username, new User(username, password)) == null;
	}

	/**
	 * Returns an user
	 *
	 * @param username the user username
	 *
	 * @return the user with that username
	 *
	 * @throws InexistentUserException if the user doesn't exist
	 */
	public User get(String username) throws InexistentUserException {
		User user = users.get(username);
		if (user == null) // inexistent user
			throw new InexistentUserException("Inexistent user: " + username);

		return user;
	}
}
