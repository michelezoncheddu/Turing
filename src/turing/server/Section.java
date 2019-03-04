package turing.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Section {
	private String path;
	private User editingUser = null; // which is currently editing

	/**
	 * Creates a new section
	 */
	public Section(String path) {
		this.path = path;
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
	 * Unlocks the section
	 */
	public synchronized void endEdit(User user) {
		if (editingUser == user)
			editingUser = null;
	}

	/**
	 * Returns the content of the section
	 */
	public String getContent() throws IOException {
		Path path = Paths.get(this.path);
		StringBuilder data = new StringBuilder();
		List<String> list = Files.readAllLines(path);
		for (String line : list) {
			data.append(line);
			data.append(System.lineSeparator());
		}
		return data.toString(); // TODO: cache data?
	}
}
