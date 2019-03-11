package turing.client;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Thread for listening chat messages
 */
public class ChatListener implements Runnable {
	private InetAddress chatAddress;
	private JTextArea chatArea;
	private DatagramChannel channel;

	/**
	 * Creates a new chat listener
	 *
	 * @param chatAddress the address to listen
	 * @param chatArea    the chat messages text area
	 */
	public ChatListener(InetAddress chatAddress, JTextArea chatArea) {
		this.chatAddress = chatAddress;
		this.chatArea = chatArea;
	}

	@Override
	public void run() {
		try {
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName("localhost"));
			channel = DatagramChannel.open();
			channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
			channel.bind(new InetSocketAddress(Client.CHAT_PORT));
			channel.join(chatAddress, networkInterface);
		} catch (IOException e) {
			Client.frame.showErrorDialog(e.getMessage());
			return;
		}

		while (true) {
			ByteBuffer byteBuffer = ByteBuffer.allocate(1024); // TODO: enough?
			byteBuffer.clear();
			try {
				channel.receive(byteBuffer);
			} catch (IOException e) {
				break;
			}
			byteBuffer.flip();
			chatArea.append(new String(byteBuffer.array()).trim());
			chatArea.append("\n");
		}
	}

	/**
	 * Terminates the thread
	 */
	public void shutdown() {
		try {
			channel.close();
		} catch (IOException e) {
			Client.frame.showErrorDialog(e.getMessage());
		}
	}
}
