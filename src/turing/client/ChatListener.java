package turing.client;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ChatListener implements Runnable {

	@Override
	public void run() {
		while (true) {
			try {
				InetAddress groupAddress = InetAddress.getByName("239.0.0.0");

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
				System.out.println("Messaggio ricevuto:" + new String(byteBuffer.array()).trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
