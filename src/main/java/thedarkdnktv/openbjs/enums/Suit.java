package thedarkdnktv.openbjs.enums;

/**
 * Enum of all presented suits, contains a unicode character of it (for display purpuses)
 * @author TheDarkDnKTv
 *
 */
public enum Suit {
	CLUBS	("♣"),
	DIAMONDS("♦"),
	HEARTS	("♥"),
	SPADES	("♠");
	
	public final String symbol;
	
	Suit(String symbol) {
		this.symbol = symbol;
	}
}
