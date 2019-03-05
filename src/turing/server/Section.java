package turing.server;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class Section {
	private Path path;
	private User editingUser = null; // which is currently editing

	/**
	 * Creates a new section
	 */
	public Section(String path) {
		this.path = Paths.get(path);
	}

	/**
	 * Locks the section
	 */
	public synchronized boolean startEdit(User user) {
		if (editingUser == null) {
			editingUser = user;
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
		return data.toString(); // TODO: cache data?
	}
}
