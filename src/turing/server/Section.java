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
	 * TO DO
	 */
	public Section(String path) {
		this.path = path;
	}

	/**
	 * TO DO
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
	 * TO DO
	 */
	public String getContent() throws IOException {
		Path path = Paths.get(this.path);
		StringBuilder data = new StringBuilder();
		List<String> list;
		list = Files.readAllLines(path);
		for (String line : list) {
			data.append(line);
			data.append(System.lineSeparator());
		}
		return data.toString(); // TODO: cache data?
	}
}
