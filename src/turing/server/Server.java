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
	private final int RMI_PORT = 1099;
	private final String SERVER_NAME = "TURING_SERVER";
	private final int DEFAULT_PORT = 1100;
	private boolean stop = false;

	/**
	 * Initialize the server.
	 */
	@Override
	public void run() {
		UserManager userManager = new UserManager();
		exportObject(userManager);
		ClientHandler.setUserManager(userManager);

		ServerSocket serverSocket;
		Socket clientSocket;

		// initialize the thread pool
		ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		try {
			serverSocket = new ServerSocket(DEFAULT_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		out.println("Server ready, waiting for connections...");

		// waiting for connections loop
		while (!stop) {
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				if (stop)
					break;
				throw new RuntimeException("Error accepting client connection", e);
			}
			threadPool.execute(new ClientHandler(clientSocket));
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
