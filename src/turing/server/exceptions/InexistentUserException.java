package turing.server.exceptions;

/**
 * Exception for a non registered user login attempt
 */
public class InexistentUserException extends Exception {

	/**
	 * Creates a new exception
	 *
	 * @param message the exception message
	 */
	public InexistentUserException(String message) {
		super(message);
	}
}
