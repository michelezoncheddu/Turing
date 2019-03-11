package turing.server.exceptions;

/**
 * Exception for an inexistent document request
 */
public class InexistentDocumentException extends Exception {

	/**
	 * Creates a new exception
	 *
	 * @param message the exception message
	 */
	public InexistentDocumentException(String message) {
		super(message);
	}
}
