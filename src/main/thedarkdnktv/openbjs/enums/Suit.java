package thedarkdnktv.openbjs.enums;

public enum Suit {
	CLUBS	("♣"),
	DIAMONDS("♦"),
	HEARTS	("♥"),
	SPADES	("♠");
	
	public final String SYMBOL;
	
	Suit(String symbol) {
		SYMBOL = symbol;
	}
}
