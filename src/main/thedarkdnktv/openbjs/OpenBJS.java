package thedarkdnktv.openbjs;

import java.util.ArrayList;
import java.util.Queue;

import thedarkdnktv.openbjs.network.NetHandler;

/**
 * @author TheDarkDnKTv
 *
 */
public class OpenBJS {
	public static final boolean DEBUG = true;
	public static final String RANDOMORG_API_KEY = "7b57e9cc-6ebe-4c05-9456-a47e4e02ac62";
	
	public static final int DECK_SIZE = 52;
	public static final int DECKS = 8;
	public static final int CARDS = DECKS * DECK_SIZE;
	public static NetHandler net = new NetHandler();
	
	public static void main(String[] args) throws Throwable {
		Queue<Card> shoe = Shuffler.getNewShoe();
		
		info("New shoe: " + shoe.size() + " " + shoe);
		
		shoe = Shuffler.chemmyShuffle(shoe);
		info("Chemmy shuffle: " + shoe.size() + " " + shoe);
		
		shoe = Shuffler.shuffle(shoe);
		info("Shuffle: " + shoe.size() + " " + shoe);
		info("Cutting card: " + new ArrayList<>(shoe).indexOf(Card.CUTTING_CARD));
	}
	
	protected static void info(Object o) {
		System.out.println(o == null ? "null" : o.toString());
	}
	
	protected static int ceil(double value) {
		return (int) Math.ceil(value);
	}
}
