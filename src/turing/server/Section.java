package turing.server;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class Section {
	private final Document parent;
	private Path path;
	private User editingUser = null; // which is currently editing

	/**
	 * Creates a new section
	 */
	public Section(Document parent, String path) {
		this.parent = parent;
		this.path = Paths.get(path);
	}

	/**
	 * Returns the parent document
	 */
	public Document getParent() { return parent; }

	/**
	 * Locks the section
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
	 */
	public synchronized void endEdit(User user, String content) throws IOException {
		if (editingUser != user)
			return;

		editingUser = null; // unlock section
		parent.removeEditingUser();

		if (content == null) // discard changes
			return;

		Files.writeString(path, content, StandardOpenOption.TRUNCATE_EXISTING);
	}

	/**
	 * Returns the content of the section
	 */
	public String getContent() throws IOException {
		StringBuilder data = new StringBuilder();
		List<String> list = Files.readAllLines(path);
		for (String line : list) {
			data.append(line);
			data.append(System.lineSeparator());
		}
		return data.toString();
	}
}
