package turing.client;

import java.io.BufferedReader;
import java.io.IOException;

public class BackgroundListener implements Runnable {
	private BufferedReader reader;

	/**
	 * TO DO
	 */
	public BackgroundListener(BufferedReader reader) { this.reader = reader; }

	/**
	 * TO DO
	 */
	public void run() {
		while (true) {
			String read;
			try {
				read = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			System.out.println(read);
		}
	}
}
