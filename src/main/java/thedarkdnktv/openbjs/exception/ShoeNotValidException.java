package thedarkdnktv.openbjs.exception;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class ShoeNotValidException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8866183958375870990L;
	
	public ShoeNotValidException() {
		super();
	}
	
	public ShoeNotValidException(String msg) {
		super(msg);
	}
	
	public ShoeNotValidException(Throwable cause) {
		super(cause);
	}
	
	public ShoeNotValidException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
