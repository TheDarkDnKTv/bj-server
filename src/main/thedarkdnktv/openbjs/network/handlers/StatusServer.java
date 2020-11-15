package thedarkdnktv.openbjs.network.handlers;

import thedarkdnktv.openbjs.OpenBJS;
import thedarkdnktv.openbjs.api.network.NetworkHandler;
import thedarkdnktv.openbjs.manage.TableManager;
import thedarkdnktv.openbjs.network.handlers.interfaces.IStatusServer;
import thedarkdnktv.openbjs.network.packet.C_Ping;
import thedarkdnktv.openbjs.network.packet.C_ServerQuery;
import thedarkdnktv.openbjs.network.packet.S_Pong;
import thedarkdnktv.openbjs.network.packet.S_ServerQuery;

public class StatusServer implements IStatusServer {
	
	private static final String EXIT_MESSAGE = "Status request has been handled";
	private final OpenBJS server;
	private final NetworkHandler networkManager;
	private boolean handled;
	
	public StatusServer(OpenBJS server, NetworkHandler manager) {
		this.server = server;
		this.networkManager = manager;
		this.handled = false;
	}
	
	@Override
	public void onDisconnection(String reason) {}

	@Override
	public void processPing(C_Ping packet) {
		this.networkManager.sendPacket( new S_Pong(packet.getClientTime()));
		this.networkManager.closeChannel(EXIT_MESSAGE);
	}

	@Override
	public void processServerQuery(C_ServerQuery packet) {
		if (this.handled) {
			this.networkManager.closeChannel(EXIT_MESSAGE);
		} else {
			this.handled = true;
			TableManager manager = server.getTableManager();
			this.networkManager.sendPacket(new S_ServerQuery(manager.activeTableCount(), manager.totalTablesCount()));
		}
	}
}
