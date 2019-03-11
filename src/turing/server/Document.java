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
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a document inside the server
 */
public class Document {
	private String name;
	private User creator;
	private Section[] sections;
	private List<User> allowedUsers = new LinkedList<>();
	private InetAddress chatAddress = null;
	private int editingUsers = 0;

	private InetSocketAddress groupAddress;
	private DatagramChannel channel;

	/**
	 * Creates a new document
	 *
	 * @param name     the document name
	 * @param creator  the document creator
	 * @param sections the number of sections
	 *
	 * @throws IOException                  if a disk error occurs
	 * @throws PreExistentDocumentException if the document already exists
	 */
	public Document(String name, User creator, int sections) throws IOException, PreExistentDocumentException {
		this.name = name;
		this.creator = creator;
		this.sections = new Section[sections]; // TODO: sections may be too little or too big

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

	/**
	 * Returns the document name
	 *
	 * @return the document name
	 */
	public String getName() { return name; }

	/**
	 * Returns the document creator
	 *
	 * @return the document creator
	 */
	public User getCreator() { return creator; }

	/**
	 * Returns the number of sections
	 *
	 * @return the number of sections
	 */
	public int getNumberOfSections() { return sections.length; }

	/**
	 * Returns the chat address
	 *
	 * @return the chat address
	 */
	public InetAddress getChatAddress() { return chatAddress; }

	/**
	 * Checks if the document is shared
	 *
	 * @return true  if the document is shared
	 *         false otherwise
	 */
	public boolean isShared() { return !allowedUsers.isEmpty(); }

	/**
	 * Checks if the document is shared with a specific user
	 *
	 * @param user the user to search
	 *
	 * @return true if the document is shared with the user
	 *         false otherwise
	 */
	public boolean isSharedWith(User user) {
		return allowedUsers.contains(user);
	}

	/**
	 * Adds an editing user and eventually starts the chat
	 */
	public synchronized void addEditingUser() {
		editingUsers++;
		if (chatAddress == null) // first editing user joined
			openChat(AddressManager.createAddress());
	}

	/**
	 * Removes an editing user and eventually closes the chat
	 */
	public synchronized void removeEditingUser() {
		editingUsers--;
		if (editingUsers == 0) { // last editing user left
			closeChat();
		}
	}

	/**
	 * Returns the specified section
	 *
	 * @param index the number of the section to get
	 *
	 * @return the section, if index is in a valid range
	 *         null otherwise
	 */
	public Section getSection(int index) {
		if (index >= 0 && index < sections.length)
			return sections[index];
		return null;
	}

	/**
	 * Checks if the user is allowed to edit the document
	 *
	 * @param user the user to to check
	 *
	 * @return true if the user is allowed
	 *         false otherwise
	 */
	public boolean isEditableBy(User user) {
		return user == creator || allowedUsers.contains(user);
	}

	/**
	 * Share the document with another user
	 *
	 * @param user the user to share with
	 */
	public synchronized void shareWith(User user) {
		if (user != creator && !allowedUsers.contains(user)) // if not already shared
			allowedUsers.add(user);
	}

	/**
	 * Sends a message to the chat channel
	 *
	 * @param message the message to send
	 *
	 * @throws IOException if a network error occurs
	 */
	public void sendMessage(String message, String username) throws IOException {
		String toSend = username + ": " + message;
		ByteBuffer buffer = ByteBuffer.allocate(toSend.length());
		buffer.put(toSend.getBytes());
		buffer.flip();
		// while (buffer.hasRemaining())
		channel.send(buffer, groupAddress); // TODO: try with big messages
	}

	/**
	 * Initializes the chat channel
	 *
	 * @param chatAddress the address of the chat
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
}
