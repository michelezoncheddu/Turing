package turing.server;

import turing.server.exceptions.InexistentDocumentException;
import turing.server.exceptions.UserNotAllowedException;

import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentManager {
	private static AbstractMap<String, Document> documents = new ConcurrentHashMap<>();

	private DocumentManager() {}

	/**
	 * TO DO
	 */
	public static void add(Document document) {
		documents.putIfAbsent(createKey(document), document);
	}

	/**
	 * TO DO
	 */
	public static Document get(User user, String creator, String docName)
			throws UserNotAllowedException, InexistentDocumentException {
		Document document = documents.get(createKey(creator, docName));
		if (document == null)
			throw new InexistentDocumentException(docName);

		// check permissions
		if (document.isEditableBy(user))
			return document;
		else
			throw new UserNotAllowedException(user.getUsername());
	}

	/**
	 * TO DO
	 */
	private static String createKey(Document document) {
		return document.getCreator().getUsername() + document.getName();
	}

	/**
	 * TO DO
	 */
	private static String createKey(String creator, String docName) {
		return creator + docName;
	}
}
