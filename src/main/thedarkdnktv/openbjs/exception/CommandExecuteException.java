package thedarkdnktv.openbjs.exception;

/**
 *
 * @author TheDarkDnKTv
 *
 */
public class CommandExecuteException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1189915436406594216L;

	
	public CommandExecuteException() {}

	public CommandExecuteException(String message) {
		super(message);
	}

	public CommandExecuteException(Throwable cause) {
		super(cause);
	}

	public CommandExecuteException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandExecuteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
