package turing.server;

import turing.UserManagerAPI;

import java.rmi.*;
import java.rmi.server.*;
import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a concurrent user manager
 */
public class UserManager extends RemoteServer implements UserManagerAPI {
	private AbstractMap<String, User> users = new ConcurrentHashMap<>();

	/**
	 * Registers a new user
	 *
	 * @param username the user username
	 * @param password the user password
	 *
	 * @return true if has been possibile to register the user (username not registered yet)
	 *         false otherwise
	 *
	 * @throws RemoteException if a RMI communication error occurs
	 */
	public boolean signUp(String username, String password) throws RemoteException {
		return users.putIfAbsent(username, new User(username, password)) == null; // TODO: check for '/' character
	}

	/**
	 * Logs in an user
	 *
	 * @param username the user username
	 * @param password the user password
	 *
	 * @return the user, if has been possible to log in (user registered and not already logged)
	 *         null otherwise
	 */
	public User logIn(String username, String password) {
		User user = users.get(username);
		if (user == null) // inexistent user
			return null;

		if (user.getPassword().equals(password))
			if (user.setOnline(true)) // setOnline is thread safe
				return user;

		return null; // wrong password or user already logged TODO: throw specific exception
	}

	/**
	 * Returns an user
	 *
	 * @param username the user username
	 *
	 * @return the user with that username, if exists
	 *         null otherwise
	 */
	public User get(String username) {
		return users.get(username);
	}
}
