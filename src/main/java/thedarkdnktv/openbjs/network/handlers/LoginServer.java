package thedarkdnktv.openbjs.network.handlers;

import java.net.InetSocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thedarkdnktv.openbjs.OpenBJS;
import thedarkdnktv.openbjs.api.interfaces.ITickable;
import thedarkdnktv.openbjs.api.network.NetworkHandler;
import thedarkdnktv.openbjs.game.Player;
import thedarkdnktv.openbjs.network.handlers.interfaces.ILoginServer;
import thedarkdnktv.openbjs.network.packet.C_LoginStart;
import thedarkdnktv.openbjs.network.packet.S_Disconnect;

/**
 * @author TheDarkDnKTv
 *
 */
public class LoginServer implements ILoginServer, ITickable {
	
	private static final Logger logger = LogManager.getLogger();
	
	private final OpenBJS server;
	private final NetworkHandler handler;
	
	private String playerName;
	private State state;
	private int connectionTimer;
	
	public LoginServer(OpenBJS server, NetworkHandler handler) {
		this.server = server;
		this.state = State.HELLO;
		this.handler = handler;
	}
	
	@Override
	public void onDisconnection(String reason) {
		logger.info("{} lost connection: {}", connectionInfo(), reason);
	}

	@Override
	public void processLoginStart(C_LoginStart packet) {
		if (state != State.HELLO) throw new IllegalStateException("Unexpected hello packet");
		playerName = packet.getLogin();
		state = State.READY_TO_ACCEPT;
	}

	@Override
	public void update() {
		if (state == State.READY_TO_ACCEPT) {
			String msg = server.getPlayerManager().allowConnect(playerName, handler);
			if (msg == null) {
				Player player = server.getPlayerManager().getPlayerFor(playerName, handler);
				InetSocketAddress addr = ((InetSocketAddress)handler.getRemoteAddress());
				logger.info("{} ({}:{}) has joined server", player.getUsername(), addr.getAddress().getHostAddress(), addr.getPort());
				
				state = State.ACCEPTED;
			} else {
				this.disconnect(msg);
			}
		}
		
		if (this.connectionTimer++ >= 560) {
			this.disconnect("Slow login");
		}
	}
	
	public void disconnect(String reason) {
		try {
			logger.info("Disconnecting: {}: {}", connectionInfo(), reason);
			this.handler.sendPacket(new S_Disconnect(reason));
			this.handler.closeChannel(reason);
		} catch (Throwable e) {
			logger.error("Error whilst disconnecting player", e);
		}
	}
	
	private String connectionInfo() {
		String address = String.valueOf(handler.getRemoteAddress());
		return playerName != null ? playerName + " (" + address + ")" : address;
	}
	
	static enum State {
		HELLO,
		READY_TO_ACCEPT,
		ACCEPTED;
	}
}
