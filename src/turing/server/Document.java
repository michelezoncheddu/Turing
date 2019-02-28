package turing.server;

import turing.server.exceptions.PreExistentDocumentException;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class Document {
	private String name;
	private User creator;
	private Section[] sections;
	private ArrayList<User> allowedUsers;
	private InetAddress multicastAddress;

	/**
	 * TO DO
	 */
	public Document(String name, User creator, int sections) throws IOException, PreExistentDocumentException {
		this.name = name;
		this.creator = creator;
		this.sections = new Section[sections]; // TODO: sections may be too little or too big
		allowedUsers = new ArrayList<>();
		String creatorName = creator.getUsername();

		// creating directory
		String dirPath = Server.ROOT + File.separator + creatorName + File.separator + name;
		File file = new File(dirPath);
		if (file.exists())
			throw new PreExistentDocumentException(dirPath);
		if (!file.mkdirs())
			throw new IOException("mkdirs " + dirPath + " failed");

		// creating sections
		String sectionPath;
		for (int i = 1; i <= sections; i++) {
			sectionPath = dirPath + File.separator + i;
			this.sections[i - 1] = new Section(sectionPath);
			file = new File(sectionPath);
			if (!file.createNewFile())
				throw new IOException("createNewFile " + sectionPath + " failed");
		}
	}

	public String getName() { return name; }
	public User getCreator() { return creator; }
	public int getNumberOfSections() { return sections.length; }

	/**
	 * TO DO
	 */
	public Section getSection(int index) {
		if (index >= 0 && index < sections.length)
			return sections[index];
		return null;
	}

	/**
	 * TO DO
	 */
	public boolean isEditableBy(User user) {
		return user == creator || allowedUsers.contains(user); // TODO: more secure checking only usernames?
	}
}
