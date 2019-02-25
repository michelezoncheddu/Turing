package turing.server;

import turing.UserManagerAPI;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

/**
 * Implements the turing server.
 */
public class Server implements Runnable {
	private final String SERVER_NAME = "TURING_SERVER";
	private final int RMI_PORT        = 1099;
	private final int DEFAULT_PORT    = 1100;
	private final int BACKGROUND_PORT = 1101;
	private boolean stop = false; // TODO: it never stops

	public static final String ROOT = "docs";

	/**
	 * Initialize the server.
	 */
	@Override
	public void run() {
		DocumentManager documentManager = new DocumentManager();
		UserManager userManager = new UserManager();
		exportObject(userManager);
		ClientHandler.setManagers(documentManager, userManager);

		ServerSocket serverSocket, backgroundSocket;
		Socket clientConnection, backgroundConnection;

		// initialize the thread pool
		ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		try {
			serverSocket = new ServerSocket(DEFAULT_PORT);
			backgroundSocket = new ServerSocket(BACKGROUND_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		out.println("Server ready, waiting for connections...");

		// waiting for connections loop
		while (!stop) {
			try {
				clientConnection = serverSocket.accept();
				backgroundConnection = backgroundSocket.accept();
			} catch (IOException e) {
				if (stop)
					break;
				throw new RuntimeException("Error accepting client connection", e);
			}
			threadPool.execute(new ClientHandler(clientConnection, backgroundConnection));
		}
		out.println("Server stopped");

		// closing defaultConnection
		try {
			serverSocket.close();
			backgroundSocket.close();
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
	 * Exports the remote object.
	 */
	private void exportObject(UserManagerAPI remote) {
		try {
			// exporting the object
			UserManagerAPI stub = (UserManagerAPI) UnicastRemoteObject.exportObject(remote, 0);

			// publishing the stub into the registry
			LocateRegistry.createRegistry(RMI_PORT);
			Registry r = LocateRegistry.getRegistry(RMI_PORT);
			r.rebind(SERVER_NAME, stub);
		} catch (RemoteException e) {
			e.printStackTrace(); // TODO: System.exit?
		}
	}

	/**
	 * Main function.
	 */
	public static void main(String[] args) {
		new Thread(new Server()).start();
	}
}
