package thedarkdnktv.openbjs.util;

import java.util.List;
import java.util.stream.Collectors;

import thedarkdnktv.openbjs.enums.Rank;
import thedarkdnktv.openbjs.game.Card;
import thedarkdnktv.openbjs.game.Hand;

public class CardUtils {
	
	public static boolean hasAce(Hand hand) {
		for (Rank rank : hand.getCardsVisible().stream().map(c -> c.getRank()).collect(Collectors.toList())) {
			if (rank == Rank.ACE) return true;
		}
		
		return false;
	}
	
	public static boolean has10(Hand hand) {
		for (Rank rank : hand.getCardsVisible().stream().map(c -> c.getRank()).collect(Collectors.toList())) {
			if (rank.value == 10) return true;
		}
		
		return false;
	}
	
	public static boolean isBlackjack(Hand hand) {
		return hand.getCardsVisible().size() == 2 && has10(hand) && hasAce(hand);
	}
	
	public static boolean isPairOfAces(Hand hand) {
		List<Card> cards = hand.getCardsVisible();
		if (cards.size() == 2) {
			return cards.get(0).getRank() == Rank.ACE && cards.get(1).getRank() == Rank.ACE;
		}
		
		return false;
	}
}
