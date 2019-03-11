package turing.client;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Thread for listening chat messages
 */
public class ChatListener implements Runnable {
	private InetAddress groupAddress;

	/**
	 * Creates a new chat listener
	 *
	 * @param address the address to listen
	 */
	public ChatListener(String address) {
		try {
			groupAddress = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			groupAddress = null;
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName("localhost"));
				DatagramChannel channel = DatagramChannel.open();
				channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
				channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
				channel.bind(new InetSocketAddress(Client.CHAT_PORT));
				channel.join(groupAddress, networkInterface);

				ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
				byteBuffer.clear();
				channel.receive(byteBuffer);
				byteBuffer.flip();
				System.out.println(new String(byteBuffer.array()).trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
