package turing.server;

import turing.ServerNotificationManagerAPI;
import turing.UserManagerAPI;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
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
	static final String DOCS_ROOT = "docs"; // documents folder
	static final int    CHAT_PORT = 1101;   // multicast port
	static final int    MTU       = 1500;   // Ethernet MTU

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

		System.setProperty("java.net.preferIPv4Stack", "true");

		ServerSocket serverSocket;
		Socket clientConnection;

		try {
			serverSocket = new ServerSocket(DEFAULT_PORT);
		} catch (IOException e) {
			e.printStackTrace(); // cannot create socket
			return;
		}

		// initialize the thread pool
		ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

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

		// closing socket
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
	 *
	 * @param userManager         the user manager to export
	 * @param notificationManager the notification manager to export
	 */
	private void exportObjects(UserManagerAPI userManager, ServerNotificationManagerAPI notificationManager) {
		int    RMI_PORT            = 1099;
		String REGISTRATION_OBJECT = "reg";
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
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace(); // TODO?
		}
	}

	/**
	 * Main function
	 */
	public static void main(String[] args) {
		new Thread(new Server()).start();
	}
}
