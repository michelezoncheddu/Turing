package turing.server.exceptions;

/**
 * Exception for login attempts with the user already logged
 */
public class AlreadyLoggedException extends Exception {

	/**
	 * Creates a new exception
	 *
	 * @param message the exception message
	 */
	public AlreadyLoggedException(String message) {
		super(message);
	}
}
