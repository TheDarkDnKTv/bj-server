package thedarkdnktv.openbjs.api.interfaces;

import java.lang.Thread.UncaughtExceptionHandler;

/** A server interface, to put server instance to API class
 * @author TheDarkDnKTv
 *
 */
public interface IServer extends Runnable {
	
	/** 
	 * Determines if server is running
	 */
	public boolean isRunning();
	
	/**
	 * Calling this method will stop server kernel
	 */
	public void stop();
	
	/**
	 * Server instance should have a main exception handler. <br>
	 * Using in thread generators
	 */
	public UncaughtExceptionHandler getExceptionHandler();
}
