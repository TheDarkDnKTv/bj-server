package thedarkdnktv.openbjs.game;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class encapsulating gaming table
 * @author TheDarkDnKTv
 *
 */
public class Table {
	private static final Logger log = LogManager.getLogger();
	
	private final Box[] boxes;
	private final Hand dealerHand;
	
	private Shoe shoe;
	private List<Card> holder;
	private State state = State.DISABLED;
	
	private int ID = -1;
	private String lobbyName = null;
	
	/** Betting time in milliseconds */
	private int BETTING_TIME = 4000;
	
	
	/**
	 * @param boxes min - 3; max - 9
	 * @param shoe
	 */
	public Table(int boxes) {
		if (boxes < 3 || boxes > 9)
			throw new IllegalArgumentException("Wrong boxes amount: " + boxes);
		this.boxes = new Box[boxes];
		this.shoe = new Shoe();
		this.dealerHand = new Hand();
		this.holder = new ArrayList<>();
		for (int i = 0; i < this.boxes.length; i++)
			this.boxes[i] = new Box();
	}
	
	/**
	 * Main update method
	 */
	public void update() {
		if (state != State.DISABLED) {
			if (shoe.isNew()) {
//				log.info("Shoe is new, there is no cards. Proceed to create and shuffle");
			}
			
			
		}
	}
	
	public void launch() {
		if (state == State.DISABLED) {
			state = State.WAITING_FOR_BETS;
		}
	}
	
	public void notifyUpdate() {
		for (Box box : boxes) {
			box.thePlayer.getHandler().sendData(null); // TODO
		}
	}
	
	/*
	 * SETTERS
	 */
	
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
	
	public void setLobbyName(String newName) {
		if (newName != null && !newName.isEmpty()) {
			this.lobbyName = newName;
		}
	}
	
	public void setID(int id) {
		this.ID = id;
	}
	
	/*
	 * GETTERS
	 */
	
	public String getLobbyName() {
		return lobbyName == null ? "Table#" + ID : lobbyName;
	}
	
	public State getState() {
		return state;
	}

	
	public static class Box {
		protected Player thePlayer;
		
		public boolean isFree() {
			return thePlayer == null;
		}
	}
	
	public static enum State {
		WAITING_FOR_BETS,
		BETTING_TIME,
		IN_GAME,
		DISABLED;
	}
}
