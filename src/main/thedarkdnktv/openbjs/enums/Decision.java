package thedarkdnktv.openbjs.enums;

import java.util.List;

import thedarkdnktv.openbjs.game.Card;
import thedarkdnktv.openbjs.game.Hand;

public enum Decision {
	HIT,
	STAND,
	DOUBLE_DOWN,
	SPLIT;
	
	public boolean canPerform(Hand hand) {
		List<Card> cards = hand.getCardsVisible();
		
		switch (this) {
		case HIT:
		case DOUBLE_DOWN:
			return true;
		case SPLIT:
			return cards.size() == 2 && cards.get(0).getRank().VALUE == cards.get(1).getRank().VALUE;
		case STAND:
			return true;
		}
		
		return false;
	}
}
