package turing.server;

import turing.ServerNotificationManagerAPI;
import turing.UserManagerAPI;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

/**
 * Implements the turing server
 */
public class Server implements Runnable {
	static final String DOCS_ROOT = "docs";
	static final int    CHAT_PORT = 1101;

	// global managers
	static UserManager               userManager;
	static ServerNotificationManager notificationManager;

	/**
	 * Initializes the server
	 */
	@Override
	public void run() {
		int DEFAULT_PORT = 1100;
		boolean stop = false; // TODO: stop function
		userManager         = new UserManager();
		notificationManager = new ServerNotificationManager();
		exportObjects(userManager, notificationManager);

		ServerSocket serverSocket;
		Socket clientConnection;

		// initialize the thread pool
		ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		try {
			serverSocket = new ServerSocket(DEFAULT_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		System.setProperty("java.net.preferIPv4Stack", "true");

		out.println("Server ready, waiting for connections...");

		// waiting for connections loop
		while (!stop) {
			try {
				clientConnection = serverSocket.accept();
			} catch (IOException e) {
				if (stop)
					break;
				throw new RuntimeException("Error accepting client connection", e);
			}
			threadPool.execute(new ClientHandler(clientConnection));
		}
		out.println("Server stopped");

		// closing defaultConnection
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// await thread pool termination
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Exports the remote objects
	 */
	private void exportObjects(UserManagerAPI userManager, ServerNotificationManagerAPI notificationManager) {
		int    RMI_PORT            = 1099;
		String REGISTRATION_OBJECT = "reg"; // TODO: make shared
		String NOTIFICATION_OBJECT = "not";

		try {
			// exporting objects
			UserManagerAPI userManagerStub =
					(UserManagerAPI) UnicastRemoteObject.exportObject(userManager, 0);
			ServerNotificationManagerAPI notificationStub =
					(ServerNotificationManagerAPI) UnicastRemoteObject.exportObject(notificationManager, 0);

			Registry registry = LocateRegistry.createRegistry(RMI_PORT);

			// publishing the stubs into the registry
			registry.bind(REGISTRATION_OBJECT, userManagerStub);
			registry.bind(NOTIFICATION_OBJECT, notificationStub);
		} catch (Exception e) { // TODO: generic Exception
			e.printStackTrace(); // TODO: System.exit?
		}
	}

	/**
	 * Main function
	 */
	public static void main(String[] args) {
		new Thread(new Server()).start();
	}
}
