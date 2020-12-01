package thedarkdnktv.openbjs;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.filter.MarkerFilter;

import thedarkdnktv.openbjs.api.API;
import thedarkdnktv.openbjs.api.interfaces.IServer;
import thedarkdnktv.openbjs.manage.CommandManager;
import thedarkdnktv.openbjs.manage.TableManager;
import thedarkdnktv.openbjs.network.NetworkSystem;
import thedarkdnktv.openbjs.util.IThreadListener;

/**
 * @author TheDarkDnKTv
 *
 */
public class OpenBJS implements IServer, IThreadListener {
	private static final Logger logger = LogManager.getLogger();
	private static final UncaughtExceptionHandler HANDLER;
	
	/* Misc settings */
	public static final boolean DEBUG = true;
	public static final boolean NET_DEBUG = true;
	public static final String RANDOMORG_API_KEY = "7b57e9cc-6ebe-4c05-9456-a47e4e02ac62";
	
	/* Main Server settings */
	public static final int DECK_SIZE = 52;
	public static final int DECKS = 8;
	public static final int CARDS = DECKS * DECK_SIZE;
	public static final int TICK_PERIOD = 100;
	
	public static OpenBJS INSTANCE;
	
	private CommandManager commandManager;
	private TableManager tableManager;
	private NetworkSystem networkSystem;
	private boolean isRunning;
	private Queue<FutureTask<Object>> futureTasks;
	private Thread main;
	
	private OpenBJS() {
		// TODO init server config
		commandManager = new CommandManager();
		tableManager = new TableManager();
		networkSystem = new NetworkSystem(this);
		futureTasks = new ArrayDeque<>();
		
		
		isRunning = true;
	}
	
	public static void main(String[] args) throws Throwable {
		Thread.currentThread().setName("Server Bootstrap");
		
		// Disable netty logging
		Configurator.setLevel("io.netty", Level.WARN);
		
		if (!NET_DEBUG) {
			MarkerFilter filter = MarkerFilter.createFilter("NETWORK", Result.DENY, Result.NEUTRAL);
			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
	        ctx.getConfiguration().addFilter(filter);
		}
		
		CommandManager.init();
		new Thread(INSTANCE = new OpenBJS(), "Server Thread").start();
		
		while (INSTANCE == null || !INSTANCE.isRunning)
			Thread.sleep(50);
		
		API.init();
	}
	
	static {
		HANDLER = new UncaughtExceptionHandler() {
			private Logger log = LogManager.getLogger("CrashReporter");
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.error("Exception occured in thread " + t.getName());
				log.catching(e);
			}
		};
	}
	
	public static int ceil(double value) {
		return (int) Math.ceil(value);
	}

	@Override
	public void run() {
		main = Thread.currentThread();
		InetAddress address = null;  // TODO port & ip
		int port = 100;
		
		try {
			address = InetAddress.getLocalHost();
		} catch (Throwable e) {}
		
		logger.info("Starting OpenBJS Server");
		logger.info("Staring server on " + (address == null ? "localhost" : address.getHostAddress()) + ":" + port);
		
		try {
			this.getNetworkSystem().addEndpoint(address, port);
		} catch (IOException e) {
			logger.error("**** FAILED TO BIND TO PORT!");
			logger.error("Excpetion was: {}", e.toString());
			return;
		}
		
		API.initClients();
		API.runClients(INSTANCE);
		
		logger.info("Done. Type '?' or 'help' to show command list");
		try {
//			long time = System.currentTimeMillis();
			while (isRunning) {
				long execution = System.currentTimeMillis();
				this.tick();

//				if (System.currentTimeMillis() - time >= 5000) {
//					if (tableManager.hasTables()) {
//						logger.info("Activated " + tableManager.activeTableCount() + " tables");
//					} else {
//						logger.info("Has no activity");
//					}
//					
//					time = System.currentTimeMillis();
//				}
				
				execution = System.currentTimeMillis() - execution;
				if (execution < TICK_PERIOD) Thread.sleep(TICK_PERIOD - execution);
			}
		} catch (Throwable e) {
			logger.catching(e);
		}
	}
	
	private void tick() {
		commandManager.executePendingCommands();
		networkSystem.networkTick();
		this.executeTasks();
		tableManager.updateTables();
	}
	
	private void executeTasks() {
		synchronized (futureTasks) {
			while (!futureTasks.isEmpty()) {
				this.tryExecuteTask(futureTasks.poll());
			}
		}
	}
	
	private void tryExecuteTask(FutureTask<Object> task) {
		try {
			task.run();
		} catch (Throwable e) {
			logger.debug("Exception occured execution task");
			logger.catching(Level.DEBUG, e);
		}
	}
	
	public TableManager getTableManager() {
		return tableManager;
	}
	
	public NetworkSystem getNetworkSystem() {
		return networkSystem;
	}
	
	@Override
	public void stop() {
		logger.info("Stopping the server...");
		this.isRunning = false;
		if (this.getNetworkSystem() != null) {
			this.getNetworkSystem().terminateEndpoints();
		}
	}
	
	@Override
	public UncaughtExceptionHandler getExceptionHandler() {
		return HANDLER;
	}
	
	@Override
	public boolean isRunning() {
		return this.isRunning;
	}

	@Override
	public Future<Object> scheduleTask(Runnable executable) {
		Objects.requireNonNull(executable);
		synchronized (futureTasks) {
			FutureTask<Object> task = new FutureTask<>(executable, null);
			futureTasks.offer(task);
			
			return task;
		}
	}

	@Override
	public boolean calledFromProperThread() {
		return Thread.currentThread() == main;
	}
}
