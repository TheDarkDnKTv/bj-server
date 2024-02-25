package thedarkdnktv.openbjs.manage;

import java.util.ArrayList;
import java.util.List;

import thedarkdnktv.openbjs.OpenBJS;
import thedarkdnktv.openbjs.api.network.NetworkHandler;
import thedarkdnktv.openbjs.game.Player;

/**
 * @author TheDarkDnKTv
 *
 */
public class PlayerRegistry {
	private OpenBJS server;
	private List<Player> players;
	
	
	public PlayerRegistry(OpenBJS server) {
		this.server = server;
		players = new ArrayList<>();
	}
	
	public String allowConnect(String name, NetworkHandler netHandler) {
		if (players.stream().anyMatch(p -> p.getUsername().equals(name))) {
			return "This username already occupied";
		}
		
		return null; // TODO player bans and etc
	}
	
	public Player getPlayerFor(String name, NetworkHandler handler) {
		return players.stream()
				.filter(p -> p.getUsername().equals(name))
				.findFirst().orElse(new Player(name, handler));
	}
}
