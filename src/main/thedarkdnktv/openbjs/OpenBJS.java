package thedarkdnktv.openbjs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;

import thedarkdnktv.openbjs.api.API;
import thedarkdnktv.openbjs.game.Card;
import thedarkdnktv.openbjs.game.Shuffler;
import thedarkdnktv.openbjs.game.Table;
import thedarkdnktv.openbjs.game.Table.Box;
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
	
	public static NetHandler net = new NetHandler();
	public static OpenBJS INSTANCE;
	
	private CommandManager commandManager;
	private TableManager tableManager;
	private boolean isRunning;
	
	private OpenBJS() {
		// TODO init server config
		commandManager = new CommandManager();
		tableManager = new TableManager();
		
		info("Starting OpenBJS Server");
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
		
		Thread server = new Thread(INSTANCE = new OpenBJS(), "OpenBJS Server");
		server.start();
	}
	
	public static void info(Object o) {
		System.out.println(o == null ? "null" : o.toString());
	}
	
	public static int ceil(double value) {
		return (int) Math.ceil(value);
	}

	@Override
	public void run() {
		try {
			while (isRunning) {
				commandManager.executePendingCommands();
				
				info("running");
				Thread.sleep(500);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			// TODO
		}
	}
	
	public void stop() {
		info("Stopping the server...");
		this.isRunning = false;
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
}
