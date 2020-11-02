package thedarkdnktv.openbjs.exception;

public class WrongShuffleException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4470856968965860586L;

	public WrongShuffleException() {
		super();
	}

	public WrongShuffleException(String message) {
		super(message);
	}

	public WrongShuffleException(Throwable cause) {
		super(cause);
	}

	public WrongShuffleException(String message, Throwable cause) {
		super(message, cause);
	}
}
