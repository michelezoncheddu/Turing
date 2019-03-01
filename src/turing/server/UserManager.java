package turing.server;

import turing.CallbackHelloServerInterface;
import turing.UserManagerAPI;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains a collection of users.
 */
public class UserManager extends RemoteServer implements UserManagerAPI {
	private AbstractMap<String, User> users = new ConcurrentHashMap<>();

	/**
	 * Registers a new user.
	 */
	@Override
	public boolean signUp(String username, String password) throws RemoteException {
		return users.putIfAbsent(username, new User(username, password)) == null;
	}

	/**
	 * TO DO
	 */
	public boolean logIn(String username, String password) {
		User user = users.get(username);
		if (user == null)
			return false;

		if (user.getPassword().equals(password))
			return user.setOnline(true);

		return false;
	}

	/**
	 * TO DO
	 */
	public User getUser(String name) {
		return users.get(name);
	}
}
