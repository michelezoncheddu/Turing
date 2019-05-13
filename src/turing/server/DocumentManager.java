package turing.server;

import turing.server.exceptions.InexistentDocumentException;
import turing.server.exceptions.UserNotAllowedException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a concurrent document manager
 */
public class DocumentManager {
	/**
	 * Key: string "username" + "document name"
	 *
	 * NOTE: no need of ConcurrentHashMap, locks are in the sections
	 */
	private Map<String, Document> documents = new HashMap<>();

	/**
	 * Creates a new document manager
	 */
	public DocumentManager() {
		super();
	}

	/**
	 * Adds a new document in the collection
	 *
	 * @param document the document to add
	 */
	public void put(Document document) {
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
	public Document getAsGuest(User user, String docKey)
			throws UserNotAllowedException, InexistentDocumentException {
		Document document = get(docKey);

		// check permissions
		if (document.isEditableBy(user))
			return document;
		else
			throw new UserNotAllowedException("Permission denied");
	}

	/**
	 * Gets a document for sharing
	 *
	 * @param user    the user that asks for the document
	 * @param docKey  the document map kay
	 *
	 * @return the document searched for
	 *
	 * @throws UserNotAllowedException     if the user is not the document creator
	 * @throws InexistentDocumentException if the document doesn't exist
	 */
	public Document getAsCreator(User user, String docKey)
			throws UserNotAllowedException, InexistentDocumentException {
		Document document = get(docKey);

		// check permissions
		if (user == document.getCreator())
			return document;
		else
			throw new UserNotAllowedException("You cannot share other users' documents");
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
	private Document get(String docKey) throws InexistentDocumentException {
		Document document = documents.get(docKey);
		if (document == null)
			throw new InexistentDocumentException("Inexistent document");
		return document;
	}

	/**
	 * Creates the map key for a document
	 *
	 * @param document the document
	 *
	 * @return the key string
	 */
	public String makeKey(Document document) {
		return document.getCreator().getUsername() + File.separator + document.getName();
	}

	/**
	 * Creates the map key for a document
	 *
	 * @param creatorUsername the document creator username
	 * @param documentName    the document name
	 *
	 * @return the key string
	 */
	public String makeKey(String creatorUsername, String documentName) {
		return creatorUsername + File.separator + documentName;
	}
}
