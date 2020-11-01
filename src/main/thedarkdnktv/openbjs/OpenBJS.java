package thedarkdnktv.openbjs;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author TheDarkDnKTv
 *
 */
public class OpenBJS {
	
	public static final int CARDS = 8 * 52;
	
	public static void main(String[] args) throws Throwable {
		Collection<Card> shoe = Shuffler.getNewShoe();
		
		info("Shoe size:      " + shoe.size());
		info("New shoe:       " + shoe);
		
		shoe = Shuffler.chemmyShuffle(shoe);
		info("Chemmy shuffle: " + shoe);
		info("Cutting card: " + new ArrayList<>(shoe).indexOf(Card.CUTTING_CARD));
		
		
		shoe = Shuffler.shuffle(shoe);
		info("Shuffle:        " + shoe);
		info("Cutting card: " + new ArrayList<>(shoe).indexOf(Card.CUTTING_CARD));
	}
	
	protected static void info(Object o) {
		System.out.println(o == null ? "null" : o.toString());
	}
}
