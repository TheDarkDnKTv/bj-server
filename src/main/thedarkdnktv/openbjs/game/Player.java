package thedarkdnktv.openbjs.game;

import thedarkdnktv.openbjs.api.network.IActionHandler;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class Player {
	private String displayName = "unknown";
	private Hand mainHand;
	private Hand splittedHand;
	/**
	 * Client instance, used to send data to current player
	 */
	private IActionHandler actionHandler;
	
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
	
	public IActionHandler getHandler() {
		return actionHandler;
	}
}
