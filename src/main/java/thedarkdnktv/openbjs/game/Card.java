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
	private int deckID = 0;
	
	/** Use <b>only this</b> instance to cut off a shoe halfs */
	public static final Card CUTTING_CARD = new Card(null, null, -1);
	
	private Card(Rank rank, Suit suit, int deckID) {
		this.rank = rank;
		this.suit = suit;
		this.deckID = deckID;
	}
	
	/**
	 * The factory method
	 * @return a new single deck of cards
	 */
	public static Collection<Card> getDeck(int deckID) {
		ArrayList<Card> deck = new ArrayList<>();
		
		for (Suit suit : Suit.values()) {
			for (Rank rank : Rank.values()) {
				deck.add(new Card(rank, suit, deckID));
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
	
	public int getDeckID() {
		return deckID;
	}
	
	/*
	 * Overrrides
	 */
	@Override
	public String toString() {
		return suit == null || rank == null ? "CUTTING_CARD" : "[#" + this.deckID + "]" + this.represent();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.suit, this.rank, this.deckID);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Card crd) {
			return crd.deckID == this.deckID && crd.rank == this.rank && crd.suit == this.suit;
		}
		
		return false;
	}
}
