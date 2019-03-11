package turing.server;

import turing.UserManagerAPI;

import java.io.File;
import java.rmi.*;
import java.rmi.server.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a concurrent user manager
 */
public class UserManager extends RemoteServer implements UserManagerAPI {
	private Map<String, User> users = new ConcurrentHashMap<>();

	/**
	 * Registers a new user
	 *
	 * @param username the user username
	 * @param password the user password
	 *
	 * @return true if has been possibile to register the user (username not registered yet)
	 *         false otherwise
	 *
	 * @throws NullPointerException if username is null or password is null
	 * @throws RemoteException      if a RMI communication error occurs
	 */
	public boolean signUp(String username, String password) throws NullPointerException, RemoteException {
		if (username == null || password == null)
			throw new NullPointerException();

		if (username.contains(File.separator))
			return false;

		return users.putIfAbsent(username, new User(username, password)) == null;
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
