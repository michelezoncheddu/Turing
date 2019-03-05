package turing.client;

import java.net.InetAddress;

/**
 * Represents a document inside the client
 */
public class Document {
	private String name;
	private String creator;
	private int sections;
	private boolean shared;
	private InetAddress chatAddress;

	/**
	 * Creates a new document
	 */
	public Document(String name, String creator, int sections, boolean shared) {
		this.name = name;
		this.creator = creator;
		this.sections = sections;
		this.shared = shared;
	}

	public String getName() { return name; }
	public String getCreator() { return creator; }
	public int getSections() { return sections; }
	public boolean isShared() { return shared; }
}
