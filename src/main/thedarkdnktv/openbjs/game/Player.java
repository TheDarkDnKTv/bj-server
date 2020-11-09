package thedarkdnktv.openbjs.game;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class Player {
	private String displayName = "unknown";
	private Hand mainHand;
	private Hand splittedHand;
	
	
	public Player(String name) {
		displayName = name;
	}
	
	/*
	 * GETTERS
	 */
	public String getUsername() {
		return displayName;
	}
	
	public Hand getMainHand() {
		return mainHand;
	}
	
	public Hand getSplitHand() {
		return splittedHand;
	}
}
