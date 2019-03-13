package turing.client;

/**
 * Represents a document inside the client
 */
public class Document {
	private String name;
	private String creator;
	private int sections;
	private boolean shared;

	/**
	 * Creates a new document
	 *
	 * @param name     the document name
	 * @param creator  the creator name
	 * @param sections the number of sections
	 * @param shared   the shared status
	 */
	public Document(String name, String creator, int sections, boolean shared) {
		this.name = name;
		this.creator = creator;
		this.sections = sections;
		this.shared = shared;
	}

	/**
	 * Returns the document name
	 *
	 * @return the document name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the document creator
	 *
	 * @return the document creator
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * Returns the number of sections
	 *
	 * @return the number of sections
	 */
	public int getSections() {
		return sections;
	}

	/**
	 * Returns the shared status
	 *
	 * @return the shared status
	 */
	public boolean isShared() {
		return shared;
	}

	/**
	 * Sets the shared status
	 *
	 * @param shared the new status
	 */
	public void setShared(boolean shared) {
		this.shared = shared;
	}
}
