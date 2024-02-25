package thedarkdnktv.openbjs.api.interfaces;

/** Marks anything what should be updated
 * @author TheDarkDnKTv
 *
 */
public interface ITickable {
	
	/**
	 * This method calls every server tick
	 */
	public void update();
}
