package thedarkdnktv.openbjs.game;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import thedarkdnktv.openbjs.enums.Decision;
import thedarkdnktv.openbjs.util.CardUtils;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class Hand {
	private LinkedList<CardWrapper> cards;
	private Score score;
	private Decision lastDecision = null; // TODO may remove this in future
	
	public Hand() {
		cards = new LinkedList<>();
		score = new Score();
	}
	
	/*
	 * ACTIONS
	 */
	
	public void inititalCard(Card card, boolean hidden) {
		if (cards.size() < 2) {
			this.wrapCard(card, hidden);
		} else {
			throw new RuntimeException("Can not add initial cards anymore");
		}
	}
	
	public void hit(Card card) {
		this.wrapCard(card);
		lastDecision = Decision.HIT;
	}
	
	public void doubleDown(Card card) {
		this.wrapCard(card);
		lastDecision = Decision.DOUBLE_DOWN;
		score.setComplete();
	}
	
	public Hand split(Card first, Card second) {
		boolean splitOnAces = CardUtils.isPairOfAces(this);
		Hand handTwo = new Hand();
		handTwo.cards.add(cards.remove(1));
		this.wrapCard(first);
		handTwo.wrapCard(second);
		this.lastDecision = Decision.SPLIT;
		handTwo.lastDecision = Decision.SPLIT;
		
		if (splitOnAces) {
			this.score.setComplete();
			handTwo.score.setComplete();
		}
		
		return handTwo;
	}
	
	public void stand() {
		lastDecision = Decision.STAND;
		score.setComplete();
	}
	
	/*
	 * GETTERS
	 */
	
	public List<Card> getCardsVisible() {
		return cards.stream().map(wrap -> wrap.card).collect(Collectors.toList());
	}
	
	public Score getTotalScore() {
		return score;
	}
	
	/*
	 * INTERNAL METHODS
	 */
	
	private void wrapCard(Card card) {
		this.wrapCard(card, false);
	}
	
	private void wrapCard(Card card, boolean hidden) {
		score.apply(card);
		cards.add(new CardWrapper(card, hidden));
	}
	
	/**
	 * @author TheDarkDnKTv
	 */	
	public static class CardWrapper {
		public final boolean isHidden;
		public final Card card;
		
		public CardWrapper(Card card, boolean hidden) {
			this.isHidden = hidden;
			this.card = card;
		}
	}
}
