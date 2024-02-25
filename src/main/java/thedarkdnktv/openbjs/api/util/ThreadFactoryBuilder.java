package thedarkdnktv.openbjs.api.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class ThreadFactoryBuilder {
	private boolean deamon = false;
	private int count = 0;
	private int priority = Thread.NORM_PRIORITY;
	private String nameFormat = null;
	private UncaughtExceptionHandler handler = null;
	
	public ThreadFactoryBuilder setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
		this.handler = Objects.requireNonNull(handler);
		return this;
	}
	
	public ThreadFactoryBuilder setNameFormat(String nameFormat) {
		String.format(nameFormat, 0);
		this.nameFormat = nameFormat;
		return this;
	}
	
	public ThreadFactoryBuilder setPriority(int priority) {
		if (priority >= Thread.MIN_PRIORITY && priority <= Thread.MAX_PRIORITY) {
			this.priority = priority;
		} else {
			throw new IllegalArgumentException("Wrong thread priority: " + priority);
		}
		
		return this;
	}
	
	public ThreadFactoryBuilder setDeamon(boolean deamon) {
		this.deamon = deamon;
		return this;
	}
	
	private String getName() {
		final String format = nameFormat != null ? nameFormat : "ThreadBuilderFactory-thread-%d";
		return String.format(format, count++);
	}
	
	public ThreadFactory build() {
		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setDaemon(deamon);
				thread.setPriority(priority);
				thread.setName(ThreadFactoryBuilder.this.getName());
				if (handler != null) thread.setUncaughtExceptionHandler(handler);
				
				return thread;
			}
		};
	}
}
