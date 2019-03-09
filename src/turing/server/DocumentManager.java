package turing.server;

import turing.server.exceptions.InexistentDocumentException;
import turing.server.exceptions.UserNotAllowedException;

import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a concurrent document manager
 */
public class DocumentManager {
	private static AbstractMap<String, Document> documents = new ConcurrentHashMap<>();

	// singleton
	private DocumentManager() {}

	/**
	 * Adds a new document in the collection
	 *
	 * @param document the document to add
	 */
	public static void add(Document document) {
		documents.putIfAbsent(createKey(document), document);
	}

	/**
	 * Gets a document for a specified user
	 *
	 * @param user    the user that asks for the document
	 * @param creator the document creator
	 * @param docName the document name
	 *
	 * @return the document searched for
	 *
	 * @throws UserNotAllowedException     if the user is not allowed to get the document
	 * @throws InexistentDocumentException if the document doesn't exist
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
	 * Creates the map key for a document
	 *
	 * @param document the document
	 *
	 * @return the key string
	 */
	private static String createKey(Document document) {
		return document.getCreator().getUsername() + document.getName();
	}

	/**
	 * Creates the map key for a document
	 *
	 * @param creatorUsername the document creator username
	 * @param documentName    the document name
	 *
	 * @return the key string
	 */
	private static String createKey(String creatorUsername, String documentName) {
		return creatorUsername + documentName;
	}
}
