package thedarkdnktv.openbjs;

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import thedarkdnktv.openbjs.api.API;
import thedarkdnktv.openbjs.manage.CommandManager;
import thedarkdnktv.openbjs.manage.TableManager;
import thedarkdnktv.openbjs.network.NetworkSystem;

/**
 * @author TheDarkDnKTv
 *
 */
public class OpenBJS implements Runnable {
	public static final boolean DEBUG = true;
	public static final String RANDOMORG_API_KEY = "7b57e9cc-6ebe-4c05-9456-a47e4e02ac62";
	
	public static final int DECK_SIZE = 52;
	public static final int DECKS = 8;
	public static final int CARDS = DECKS * DECK_SIZE;
	public static final int TICK_PERIOD = 100;
	
	public static OpenBJS INSTANCE;
	
	private static Logger logger = LogManager.getLogger();
	
	private CommandManager commandManager;
	private TableManager tableManager;
	private NetworkSystem networkSystem;
	private boolean isRunning;
	
	private OpenBJS() {
		// TODO init server config
		commandManager = new CommandManager();
		tableManager = new TableManager();
		networkSystem = new NetworkSystem(this);
		
		isRunning = true;
	}
	
	public static void main(String[] args) throws Throwable {
		// Disable netty logging
		Configurator.setLevel("io.netty", Level.WARN);
		
		API.init();
		CommandManager.init();
		new Thread(INSTANCE = new OpenBJS(), "Server Thread").start();
		API.setupLocalClients();
		API.runClients();
	}
	
	public static int ceil(double value) {
		return (int) Math.ceil(value);
	}

	@Override
	public void run() {
		logger.info("Starting OpenBJS Server");
		try {
			this.getNetworkSystem().addEndpoint(null, 100); // TODO
		} catch (IOException e) {
			logger.error("**** FAILED TO BIND TO PORT!");
			logger.error("Excpetion was: {}", e.toString());
			return;
		}
		
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
		tableManager.updateTables();
		networkSystem.networkTick();
	}
	
	public void stop() {
		logger.info("Stopping the server...");
		this.isRunning = false;
		if (this.getNetworkSystem() != null) {
			this.getNetworkSystem().terminateEndpoints();
		}
	}
	
	public TableManager getTableManager() {
		return tableManager;
	}
	
	public NetworkSystem getNetworkSystem() {
		return networkSystem;
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
}
