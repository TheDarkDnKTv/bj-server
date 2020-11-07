package thedarkdnktv.openbjs.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Class encapsulating gaming table
 * @author TheDarkDnKTv
 *
 */
public class Table {
	
	private final Box[] boxes;
	private final Dealer dealer;
	
	private Queue<? extends Card> shoe;
	private List<? extends Card> holder;
	private State state = State.WAITING_FOR_BETS;
	
	/** Betting time in milliseconds */
	private int BETTING_TIME = 4000;
	
	
	/**
	 * @param boxes min - 3; max - 9
	 * @param shoe
	 */
	public Table(int boxes, Collection<? extends Card> shoe) {
		if (boxes < 3 || boxes > 9)
			throw new IllegalArgumentException("Wrong boxes amount: " + boxes);
		this.boxes = new Box[boxes];
		this.dealer = new Dealer();
		this.shoe = new ArrayBlockingQueue<>(shoe.size(), true, shoe);
		this.holder = new ArrayList<>();
		for (int i = 0; i < this.boxes.length; i++)
			this.boxes[i] = new Box();
	}
	
	public void update() {
		
	}
	
	public Box takeSeat(int idx) {
		if (isBoxInRange(idx)) {
			idx--;
			if (boxes[idx].isFree()) {
				return boxes[idx];
			}
		}
		
		return null;
	}
	
	public boolean placeBet(int box, double bet) {
		if (state == State.WAITING_FOR_BETS || state == State.BETTING_TIME) {
			if (isBoxInRange(box)) {
				Box b = boxes[--box];
				if (b.bet == 0) {
					b.bet = bet;
					// TODO fire event
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Set a betting time timeout in milliseconds
	 * @param time >= 200
	 * @return true if success
	 */
	public boolean setBettingTime(int time) {
		if (state != State.BETTING_TIME && time >= 200) {
			BETTING_TIME = time;
			return true;
		}
		
		return false;
	}
	
	private boolean isBoxInRange(int box) {
		return box > 0 && box <= boxes.length;
	}
	
	public static class Dealer extends Box {
		
		public Dealer() {
			playerName = "Dealer";
		}
	}
	
	public static class Box {
		protected String playerName = null;
		protected double bet = 0;
		protected final List<? extends Card> cards = new ArrayList<>();
		
		public void setDisplayName(String name) {
			playerName = name;
		}
		
		public List<? extends Card> getCards() {
			return Collections.unmodifiableList(cards);
		}
		
		public boolean isFree() {
			return playerName == null && bet == 0 && cards.size() == 0;
		}
	}
	
	public static enum State {
		WAITING_FOR_BETS,
		BETTING_TIME,
		IN_GAME;
	}
}
