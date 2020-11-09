package thedarkdnktv.openbjs.exception;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class CardApplyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1041173276696841272L;

	public CardApplyException() {
		super();
	}
	
	public CardApplyException(String msg) {
		super(msg);
	}
	
	public CardApplyException(Throwable cause) {
		super(cause);
	}
	
	public CardApplyException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
