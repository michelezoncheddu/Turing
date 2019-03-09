package turing.server.exceptions;

/**
 * Exception for a document request by an user not allowed to see/edit it
 */
public class UserNotAllowedException extends Exception {

	/**
	 * Creates a new exception
	 *
	 * @param message the exception message
	 */
	public UserNotAllowedException(String message) {
		super(message);
	}
}
