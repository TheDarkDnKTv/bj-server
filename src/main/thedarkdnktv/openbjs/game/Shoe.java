package thedarkdnktv.openbjs.game;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

import thedarkdnktv.openbjs.OpenBJS;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class Shoe extends ArrayBlockingQueue<Card> {

	private static final long serialVersionUID = 3407145595616178703L;
	
	private boolean valid;
	private boolean needShuffle;
	
	public Shoe() {
		super(OpenBJS.CARDS + 1, true);
		valid = false;
		needShuffle = true;
	}
	
	public Shoe(Collection<Card> cards) {
		super(OpenBJS.CARDS + 1, true, cards);
		valid = false;
		needShuffle = true;
	}
	
	public boolean validate() {
		if (Shuffler.validateShoe(this)) {
			return valid = true;
		}
		
		return false;
	}
	
	public void setShuffled() {
		this.needShuffle = false;
	}
	
	public boolean isNew() {
		return size() == 0;
	}
	
	public boolean isReady() {
		return valid && !needShuffle;
	}
	
	public boolean hasCuttingCard() {
		return contains(Card.CUTTING_CARD);
	}
	
	@Override
	public Card poll() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Card take() {
		Card card = super.poll();
		if (card == Card.CUTTING_CARD)
			needShuffle = true;
		return card;
	}
}
