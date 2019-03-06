package turing.server;

import turing.server.exceptions.PreExistentDocumentException;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;

/**
 * Represents a document inside the server
 */
public class Document {
	private String name;
	private User creator;
	private Section[] sections;
	private ArrayList<User> allowedUsers;
	private InetAddress chatAddress;
	private int editingUsers;

	private InetSocketAddress groupAddress;
	private DatagramChannel channel;

	/**
	 * Creates a new document
	 */
	public Document(String name, User creator, int sections) throws IOException, PreExistentDocumentException {
		this.name = name;
		this.creator = creator;
		this.sections = new Section[sections]; // TODO: sections may be too little or too big
		this.allowedUsers = new ArrayList<>();
		this.chatAddress = null;
		this.editingUsers = 0;

		// creating directory
		String creatorUsername = creator.getUsername();
		String dirPath = Server.DOCS_ROOT + File.separator + creatorUsername + File.separator + name;
		File file = new File(dirPath);
		if (file.exists())
			throw new PreExistentDocumentException(dirPath);
		if (!file.mkdirs())
			throw new IOException("mkdirs " + dirPath + " failed");

		// creating sections
		String sectionPath;
		for (int i = 1; i <= sections; i++) {
			sectionPath = dirPath + File.separator + i;
			this.sections[i - 1] = new Section(this, sectionPath);
			file = new File(sectionPath);
			if (!file.createNewFile())
				throw new IOException("createNewFile " + sectionPath + " failed");
		}
	}

	public String getName() { return name; }
	public User getCreator() { return creator; }
	public int getNumberOfSections() { return sections.length; }
	public InetAddress getChatAddress() { return chatAddress; }
	public boolean isShared() { return !allowedUsers.isEmpty(); }

	public synchronized void addEditingUser() {
		editingUsers++;
		if (chatAddress == null) // first editing user joined
			openChat(AddressManager.createAddress());
	}

	public synchronized void removeEditingUser() {
		editingUsers--;
		if (editingUsers == 0) { // last editing user left
			closeChat();
		}
	}

	/**
	 * Returns the specified section
	 */
	public Section getSection(int index) {
		if (index >= 0 && index < sections.length)
			return sections[index];
		return null;
	}

	/**
	 * Checks if the user is allowed to edit the document
	 */
	public boolean isEditableBy(User user) {
		return user == creator || allowedUsers.contains(user); // TODO: more secure checking only usernames?
	}

	/**
	 * Share the document with another user
	 */
	public boolean shareWith(User user) {
		if (user == creator || allowedUsers.contains(user)) // already shared
			return false;
		allowedUsers.add(user);
		return true;
	}

	/**
	 * Initializes the chat channel
	 */
	private void openChat(InetAddress chatAddress) {
		this.chatAddress = chatAddress;
		groupAddress = new InetSocketAddress(chatAddress, Server.CHAT_PORT);
		try {
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName("localhost"));
			channel = DatagramChannel.open();
			channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
		} catch (IOException e) {
			e.printStackTrace(); // TODO
		}
	}

	/**
	 * Closes the chat channel
	 */
	private void closeChat() {
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace(); // TODO
		}
		AddressManager.freeAddress(chatAddress);
		chatAddress = null;
	}

	public void sendMessage(String message) throws IOException { // TODO: are channels thread-safe?
		ByteBuffer buffer = ByteBuffer.allocate(message.length());
		buffer.put(message.getBytes());
		buffer.flip();
		while (buffer.hasRemaining())
			channel.send(buffer, groupAddress);
	}
}
