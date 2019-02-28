package turing.server;

import turing.server.exceptions.InexistentDocumentException;
import turing.server.exceptions.UserNotAllowedException;

import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentManager {
	private AbstractMap<String, Document> documents = new ConcurrentHashMap<>();

	/**
	 * TO DO
	 */
	public void add(Document document) {
		documents.putIfAbsent(createKey(document), document);
	}

	/**
	 * TO DO
	 */
	public Document get(User user, String creator, String docName)
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
	private String createKey(Document document) {
		return document.getCreator().getUsername() + document.getName();
	}

	/**
	 * TO DO
	 */
	private String createKey(String creator, String docName) {
		return creator + docName;
	}
}
