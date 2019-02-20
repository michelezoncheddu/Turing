package turing.client;

public class Document {
	private String name;
	private String creator;
	private int sections;
	// private inetaddress multicast address

	public Document(String name, String creator, int sections) {
		this.name = name;
		this.creator = creator;
		this.sections = sections;
	}

	public String getName() { return name; }

	public String getCreator() { return creator; }

	public int getSections() { return sections; }
}
