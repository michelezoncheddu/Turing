package turing.server;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**
 * Represents a section inside the server
 */
public class Section {
	private final Document parent;
	private final Path path;
	private User editingUser = null; // that is currently editing

	/**
	 * Creates a new section
	 *
	 * @param parent the parent document
	 * @param path   the file path
	 */
	public Section(Document parent, String path) {
		this.parent = parent;
		this.path = Paths.get(path);
	}

	/**
	 * Returns the parent document
	 *
	 * @return the parent document
	 */
	public Document getParent() {
		return parent;
	}

	/**
	 * Returns the editing user
	 *
	 * @return the editing user
	 */
	public User getEditingUser() {
		return editingUser;
	}

	/**
	 * Locks the section for the editing
	 *
	 * @param user the user that wants to edit the section
	 *
	 * @return true if the section has been locked for the user
	 *         false otherwhise (section already reserved)
	 */
	public synchronized boolean startEdit(User user) {
		if (editingUser == null) {
			editingUser = user;
			parent.addEditingUser();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Unlocks the section and saves the new content
	 *
	 * @param user    the user that wants to end the section editing
	 * @param content the new section content
	 *
	 * @throws IOException if a disk error occurs
	 */
	public synchronized void endEdit(User user, String content) throws IOException {
		if (editingUser != user)
			return;

		editingUser = null; // unlock section
		parent.removeEditingUser();

		if (content != null) { // save changes
			synchronized (path) { // because of getContent method
				Files.writeString(path, content, StandardOpenOption.TRUNCATE_EXISTING);
			}
		}
	}

	/**
	 * Returns the content of the section
	 *
	 * @return the section content
	 *
	 * @throws IOException if a disk error occurs
	 */
	public String getContent() throws IOException {
		StringBuilder data = new StringBuilder();
		List<String> list;
		synchronized (path) {
			list = Files.readAllLines(path);
		}
		for (String line : list) {
			data.append(line);
			data.append(System.lineSeparator());
		}
		return data.toString();
	}
}
