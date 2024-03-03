package thedarkdnktv.openbjs.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import thedarkdnktv.openbjs.core.AbstractCard;
import thedarkdnktv.openbjs.enums.*;

/**
 * Class encapsulating single card with it value
 * @author iDnK
 *
 */
public class Card extends AbstractCard {
	
	private final Rank rank;
	private final Suit suit;
	/** For debug purpuses only, to control a duplicates in shoe */
	private int deckId = 0;
	
	/** Use <b>only this</b> instance to cut off a shoe halfs */
	public static final Card CUTTING_CARD = new Card(null, null, -1);
	
	public Card(Rank rank, Suit suit, int deckId) {
		this.rank = rank;
		this.suit = suit;
		this.deckId = deckId;
	}
	
	/**
	 * The factory method
	 * @return a new single deck of cards
	 */
	public static Collection<Card> getDeck(int deckId) {
		ArrayList<Card> deck = new ArrayList<>();
		
		for (Suit suit : Suit.values()) {
			for (Rank rank : Rank.values()) {
				deck.add(new Card(rank, suit, deckId));
			}
		}
		
		return Collections.unmodifiableCollection(deck);
	}

	@Override
	public String represent() {
		if (this == CUTTING_CARD) {
			return "CUTTING_CARD";
		}

		return super.represent();
	}

	@Deprecated
	public String toStringS() {
		return this.represent();
	}
	
	/*
	 * GETTERS
	 */
	@Override
	public Rank getRank() {
		return rank;
	}

	@Override
	public Suit getSuit() {
		return suit;
	}

	@Override
	public int getId() {
		return this.deckId;
	}

	/*
	 * Overrrides
	 */
	@Override
	public String toString() {
		return suit == null || rank == null ? "CUTTING_CARD" : "[#" + this.deckId + "]" + this.represent();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.suit, this.rank, this.deckId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Card crd) {
			return crd.deckId == this.deckId && crd.rank == this.rank && crd.suit == this.suit;
		}
		
		return false;
	}
}
