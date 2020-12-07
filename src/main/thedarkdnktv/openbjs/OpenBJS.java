package thedarkdnktv.openbjs;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import thedarkdnktv.openbjs.api.API;
import thedarkdnktv.openbjs.api.interfaces.IServer;
import thedarkdnktv.openbjs.manage.CommandManager;
import thedarkdnktv.openbjs.manage.PlayerRegistry;
import thedarkdnktv.openbjs.manage.TableManager;
import thedarkdnktv.openbjs.network.NetworkSystem;
import thedarkdnktv.openbjs.util.Config;
import thedarkdnktv.openbjs.util.IThreadListener;

/**
 * @author TheDarkDnKTv
 *
 */
public class OpenBJS implements IServer, IThreadListener {
	private static final Logger logger = LogManager.getLogger();
	private static final UncaughtExceptionHandler HANDLER;
	
	/* Server settings */
	private static Config config;
	public static OpenBJS INSTANCE;
	
	private CommandManager commandManager;
	private TableManager tableManager;
	private NetworkSystem networkSystem;
	private PlayerRegistry playerRegistry;
	private boolean isRunning;
	private Queue<FutureTask<Object>> futureTasks;
	private Thread main;
	
	private OpenBJS() {
		commandManager = new CommandManager();
		tableManager = new TableManager();
		networkSystem = new NetworkSystem(this);
		playerRegistry = new PlayerRegistry(this);
		futureTasks = new ArrayDeque<>();
		config = Config.init(logger);
		
		isRunning = true;
	}
	
	public static void main(String[] args) throws Throwable {
		Thread.currentThread().setName("Server Bootstrap");
		
		// Disable netty logging
		Configurator.setLevel("io.netty", Level.WARN);
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
		
		logger.info("Starting OpenBJS Server");
		logger.info("Staring server on " + (config.getServerAddress().getHostAddress() + ":" + config.getServerPort()));
		
		try {
			this.getNetworkSystem().addEndpoint(config.getServerAddress(), config.getServerPort());
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
				if (execution < config.getTickTime()) Thread.sleep(config.getTickTime() - execution);
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
	
	public Config getServerConfig() {
		return config;
	}
	
	public PlayerRegistry getPlayerManager() {
		return playerRegistry;
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
