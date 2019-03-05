package turing.server;

import turing.UserManagerAPI;

import java.rmi.*;
import java.rmi.server.*;
import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains a collection of users
 */
public class UserManager extends RemoteServer implements UserManagerAPI {
	private AbstractMap<String, User> users = new ConcurrentHashMap<>();

	/**
	 * Registers a new user
	 */
	public boolean signUp(String username, String password) throws RemoteException {
		return users.putIfAbsent(username, new User(username, password)) == null;
	}

	/**
	 * Logs in an user
	 */
	public User logIn(String username, String password) {
		User user = users.get(username);
		if (user == null) // inexistent user
			return null;

		if (user.getPassword().equals(password))
			if (user.setOnline(true)) // setOnline is thread safe
				return user;

		return null; // wrong password or user already logged
	}
}
