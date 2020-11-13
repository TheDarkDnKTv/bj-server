package thedarkdnktv.openbjs.client;

/**
 * @author TheDarkDnKTv
 *
 */
public class ConsoleLoader {
	private boolean isAllocated;
	
	public ConsoleLoader() {
		
	}
	
	static {
		System.setProperty("java.library.path", "natives");
//		System.load("ConsoleLoader.dll");
	}
	
	/**
	 * Create a console
	 */
	private native boolean allocConsole();
	
	/**
	 * Write in console
	 */
	private native void write(byte[] bytes);
	
	/**
	 * Allocates a console
	 */
	public void createConsole() {
		isAllocated = allocConsole();
	}
	
	/**
	 * Printing in console
	 * @param str
	 */
	public void println(String str) {
		if (isAllocated) {
			this.write((str + "\n").getBytes());
		} else {
			throw new RuntimeException("Can not write in not allocated console");
		}
	}
}
