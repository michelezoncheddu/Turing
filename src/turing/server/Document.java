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
	 * @throws IllegalArgumentException     if sections is &lt; 1 or &gt; Server.MAX_SECTIONS
	 * @throws PreExistentDocumentException if the document already exists
	 */
	public Document(String name, User creator, int sections)
			throws IOException, IllegalArgumentException, PreExistentDocumentException {
		if (sections < 1 || sections > Server.MAX_SECTIONS)
			throw new IllegalArgumentException("Invalid number of sections");

		this.name = name;
		this.creator = creator;
		this.sections = new Section[sections];

		// creating directory
		String creatorUsername = creator.getUsername();
		String dirPath = Server.DOCS_ROOT + File.separator + creatorUsername + File.separator + name;
		File file = new File(dirPath);
		if (file.exists())
			throw new PreExistentDocumentException(dirPath + " already created");
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
	 * Adds an editing user and eventually starts the chat
	 * No need of synchronization because the method call is already synchronized
	 */
	public void addEditingUser() {
		editingUsers++;
		if (chatAddress == null) // first editing user joined
			openChat(Server.addressManager.createAddress());
	}

	/**
	 * Removes an editing user and eventually closes the chat
	 * No need of synchronization because the method call is already synchronized
	 */
	public void removeEditingUser() {
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
	 *
	 * @return true if the document wasn't already shared with that user
	 * 	       false otherwise
	 */
	public boolean shareWith(User user) {
		if (allowedUsers.contains(user)) // if already shared with user
			return false;

		allowedUsers.add(user);
		return true;
	}

	/**
	 * Sends a message to the chat channel
	 *
	 * @param message  the message to send
	 * @param username the sender username
	 *
	 * @return true if the message have been sent
	 *         false otherwise
	 */
	public boolean sendChatMessage(String message, String username) {
		String toSend = username + ": " + message;
		if (toSend.length() > Server.MTU)
			toSend = toSend.substring(0, Server.MTU);
		ByteBuffer buffer = ByteBuffer.allocate(toSend.length());
		buffer.put(toSend.getBytes());
		buffer.flip();
		try {
			channel.send(buffer, groupAddress);
		} catch (IOException e) {
			return false;
		}
		return true;
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
			e.printStackTrace(); // chat unavailable
		}
	}

	/**
	 * Closes the chat channel
	 */
	private void closeChat() {
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Server.addressManager.freeAddress(chatAddress);
		chatAddress = null;
	}
}
