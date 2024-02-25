package thedarkdnktv.openbjs.enums;

/**
 * Enum of all card ranks used in a game
 * @author TheDarkDnKTv
 *
 */
public enum Rank {
	ACE		(1, 	"A"	),
	TWO		(2, 	"2"	),
	THREE	(3, 	"3"	),
	FOUR	(4, 	"4"	),
	FIVE	(5, 	"5"	),
	SIX		(6, 	"6"	),
	SEVEN	(7, 	"7"	),
	EIGHT	(8, 	"8"	),
	NINE	(9,		"9"	),
	TEN		(10, 	"10"),
	JACK	(10, 	"J"	),
	QUEEN	(10, 	"Q"	),
	KING	(10, 	"K"	);
	
	public final int VALUE;
	public final String DENOMINATION;
	
	Rank(int value, String name) {
		VALUE = value;
		DENOMINATION = name;
	}
}
