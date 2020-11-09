package thedarkdnktv.openbjs.game;

import java.io.Serializable;

import thedarkdnktv.openbjs.enums.Rank;
import thedarkdnktv.openbjs.exception.CardApplyException;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class Score implements Comparable<Score>, Serializable {
	private static final long serialVersionUID = -5678779413460176785L;
	
	private boolean 
			bj = false,
			soft = false,
			tooMany = false,
			complete = false;
	private int value = 0, applyCount = 0;
	
	
	public void apply(Card card) {
		if (card == null || card == Card.CUTTING_CARD || complete) {
			throw new CardApplyException();
		}
		
		Rank cRank = card.getRank();
		value += cRank.VALUE;
		applyCount++;
		
		if (value <= 21) {
			if (cRank == Rank.ACE) {
				if (value == 11) {
					if (applyCount == 2)
						bj = true;
					value = 21;
					complete = true;
					return;
				} else if (value < 11) {
					soft = true;
				}
			}
			
			if (soft && value > 11) {
				soft = false;
			}
		} else {
			tooMany = true;
		}
	}
	
	/*
	 * SETTERS
	 */
	public void setComplete() {
		complete = true;
	}
	
	/*
	 * GETTERS
	 */
	public boolean isBlackJack() {
		return bj;
	}
	
	public boolean isSoft() {
		return soft;
	}
	
	public boolean isTooMany() {
		return tooMany;
	}
	
	public boolean canApply() {
		return !complete && !tooMany && !bj;
	}
	
	public int getScorePlain() {
		return value;
	}
	
	@Override
	public int compareTo(Score o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String toString() {
		return "";
	}
	
	@Override
	public boolean equals(Object obj) {
		return true;
	}
	
	@Override
	public int hashCode() {
		return value << 4 & 
				(bj ? 1 : 0) & 
				(soft ? 2 : 0) & 
				(tooMany ? 4 : 0) & 
				(complete ? 8 : 0);
	}
}
