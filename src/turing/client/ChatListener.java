package turing.client;

import javax.swing.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Thread for listening chat messages
 */
public class ChatListener implements Runnable {
	private InetAddress chatAddress;
	private JTextArea chatArea;
	private boolean run;

	/**
	 * Creates a new chat listener
	 *
	 * @param chatAddress the address to listen
	 * @param chatArea    the chat messages text area
	 */
	public ChatListener(InetAddress chatAddress, JTextArea chatArea) {
		this.chatAddress = chatAddress;
		this.chatArea = chatArea;
		this.run = true;
	}

	@Override
	public void run() {
		while (run) {
			try {
				NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName("localhost"));
				DatagramChannel channel = DatagramChannel.open();
				channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
				channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
				channel.bind(new InetSocketAddress(Client.CHAT_PORT));
				channel.join(chatAddress, networkInterface);

				ByteBuffer byteBuffer = ByteBuffer.allocate(1024); // enough?
				byteBuffer.clear();
				channel.receive(byteBuffer); // locking?
				byteBuffer.flip();
				chatArea.append(new String(byteBuffer.array()).trim());
			} catch (Exception e) {
				Client.frame.showErrorDialog(e.getMessage());
			}
		}
	}

	/**
	 * Terminates the thread
	 */
	public void shutdown() {
		this.run = false;
	}
}
