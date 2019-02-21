package turing.server;

import java.io.File;
import java.io.IOException;

public class Document {
	private String name;
	private User creator;
	private Section[] sections;
	// private inetaddress multicast address

	/**
	 * TO DO
	 */
	Document(String name, User creator, int sections) throws IOException {
		this.name = name;
		this.creator = creator;
		this.sections = new Section[sections]; // TODO: sections may be too little or too big
		String creatorName = creator.getUsername();

		// creating directory
		String path = "docs" + File.separator + creatorName + File.separator + name;
		File f = new File(path);
		if (!f.mkdirs())
			throw new IOException("mkdirs " + path + " failed");

		// creating sections
		for (int i = 1; i <= sections; i++) {
			this.sections[i - 1] = new Section();
			path = "docs" + File.separator + creatorName + File.separator + name + File.separator + name + "_" + i;
			f = new File(path);
			if (!f.createNewFile())
				throw new IOException("createNewFile " + path + " failed");
		}
	}

	public String getName() { return name; }

	public User getCreator() { return creator; }

	public Section[] getSections() { return sections; }
}
