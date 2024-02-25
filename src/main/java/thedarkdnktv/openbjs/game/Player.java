package thedarkdnktv.openbjs.game;

import thedarkdnktv.openbjs.api.network.NetworkHandler;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class Player {
	private String displayName = "unknown";
	public final NetworkHandler netHandler;
	
	private Hand mainHand;
	private Hand splittedHand;
	
	public Player(String name, NetworkHandler handler) {
		displayName = name;
		netHandler = handler;
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
