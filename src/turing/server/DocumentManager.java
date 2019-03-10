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
	public static void put(Document document) {
		documents.putIfAbsent(makeKey(document), document);
	}

	/**
	 * Gets a document for editing
	 *
	 * @param user    the user that asks for the document
	 * @param docKey  the document map key
	 *
	 * @return the document searched for
	 *
	 * @throws UserNotAllowedException     if the user is not allowed to get the document
	 * @throws InexistentDocumentException if the document doesn't exist
	 */
	public static Document getAsGuest(User user, String docKey)
			throws UserNotAllowedException, InexistentDocumentException {
		Document document = get(docKey);

		// check permissions
		if (document.isEditableBy(user))
			return document;
		else
			throw new UserNotAllowedException(user.getUsername());
	}

	/**
	 * Gets a document for sharing
	 *
	 * @param user    the user that asks for the document
	 * @param docKey  the document map kay
	 *
	 * @return the document searched for
	 *
	 * @throws UserNotAllowedException     if the user is not allowed to get the document
	 * @throws InexistentDocumentException if the document doesn't exist
	 */
	public static Document getAsCreator(User user, String docKey)
			throws UserNotAllowedException, InexistentDocumentException {
		Document document = get(docKey);

		// check permissions
		if (user == document.getCreator())
			return document;
		else
			throw new UserNotAllowedException(user.getUsername());
	}

	/**
	 * Gets a document
	 *
	 * @param docKey the document map key
	 *
	 * @return the document searched for
	 *
	 * @throws InexistentDocumentException if the document doesn't exist
	 */
	private static Document get(String docKey)
			throws InexistentDocumentException {
		Document document = documents.get(docKey);
		if (document == null)
			throw new InexistentDocumentException(docKey);
		return document;
	}

	/**
	 * Creates the map key for a document
	 *
	 * @param document the document
	 *
	 * @return the key string
	 */
	public static String makeKey(Document document) {
		return document.getCreator().getUsername() + "/" + document.getName();
	}

	/**
	 * Creates the map key for a document
	 *
	 * @param creatorUsername the document creator username
	 * @param documentName    the document name
	 *
	 * @return the key string
	 */
	public static String makeKey(String creatorUsername, String documentName) {
		return creatorUsername + "/" + documentName;
	}
}
