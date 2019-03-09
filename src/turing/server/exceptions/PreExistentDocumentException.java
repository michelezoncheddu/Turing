package turing.server.exceptions;

/**
 * Exception for an already existent document creation
 */
public class PreExistentDocumentException extends Exception {

	/**
	 * Creates a new exception
	 *
	 * @param message the exception message
	 */
	public PreExistentDocumentException(String message) {
		super(message);
	}
}
