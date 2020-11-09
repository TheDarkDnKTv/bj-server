package thedarkdnktv.openbjs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thedarkdnktv.openbjs.api.API;
import thedarkdnktv.openbjs.manage.CommandManager;
import thedarkdnktv.openbjs.manage.TableManager;
import thedarkdnktv.openbjs.network.NetHandler;

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
	
	public static NetHandler net = new NetHandler();
	public static OpenBJS INSTANCE;
	
	private static Logger logger = LogManager.getLogger();
	
	private CommandManager commandManager;
	private TableManager tableManager;
	private boolean isRunning;
	
	private OpenBJS() {
		// TODO init server config
		commandManager = new CommandManager();
		tableManager = new TableManager();
		
		isRunning = true;
	}
	
	public static void main(String[] args) throws Throwable {
//		Collection<? extends Card> shoe = Shuffler.getNewShoe();
//		shoe = Shuffler.chemmyShuffle(shoe);
//		shoe = Shuffler.shuffle(shoe);
//		info("Shoe: " + shoe.size() + " " + shoe.toString());
//		info("Cutting card: " + new ArrayList<>(shoe).indexOf(Card.CUTTING_CARD));
//		Table t = new Table(3, shoe);
//		Box b = t.takeSeat(2);
//		b.setDisplayName("test");
//		t.placeBet(2, 10.0);
		API.init();
		CommandManager.init();
		
		Thread server = new Thread(INSTANCE = new OpenBJS(), "Server Thread");
		server.start();
	}
	
	public static int ceil(double value) {
		return (int) Math.ceil(value);
	}

	@Override
	public void run() {
		logger.info("Starting OpenBJS Server");
		
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
	}
	
	public void stop() {
		logger.info("Stopping the server...");
		this.isRunning = false;
	}
	
	public TableManager getTableManager() {
		return tableManager;
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
}
