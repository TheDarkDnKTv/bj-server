package thedarkdnktv.openbjs.util;

import java.util.concurrent.Future;

/**
 * @author TheDarkDnKTv
 *
 */
public interface IThreadListener {
	public Future<Object> scheduleTask(Runnable executable);
	
	public boolean calledFromProperThread();
}
