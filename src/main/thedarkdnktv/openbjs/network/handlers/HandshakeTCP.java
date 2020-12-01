package thedarkdnktv.openbjs.network.handlers;

import thedarkdnktv.openbjs.OpenBJS;
import thedarkdnktv.openbjs.api.network.NetworkHandler;
import thedarkdnktv.openbjs.api.network.base.ConnectionState;
import thedarkdnktv.openbjs.network.NetworkSystem;
import thedarkdnktv.openbjs.network.handlers.interfaces.IHandshakeServer;
import thedarkdnktv.openbjs.network.packet.C_Handshake;
import thedarkdnktv.openbjs.network.packet.S_Disconnect;

/**
 * @author TheDarkDnKTv
 *
 */
public class HandshakeTCP implements IHandshakeServer {
	private final OpenBJS server;
	private final NetworkHandler networkManager;
	
	public HandshakeTCP(OpenBJS serverIn, NetworkHandler netManager) {
		this.server = serverIn;
		this.networkManager = netManager;
	}


	@Override
	public void processHandshake(C_Handshake packetIn) {
		switch(packetIn.getRequestedState()) {
		case LOGIN:
			networkManager.setConnectionState(ConnectionState.LOGIN);
			
			if (packetIn.getProtocolVerion() < NetworkSystem.PROTOCOL_VERSION) {
				String err = "Outdated client version";
				networkManager.sendPacket(new S_Disconnect(err));
				networkManager.closeChannel(err);
			} else if (packetIn.getProtocolVerion() > NetworkSystem.PROTOCOL_VERSION) {
				String err = "Outdated server version";
				networkManager.sendPacket(new S_Disconnect(err));
				networkManager.closeChannel(err);
			} else {
				networkManager.setNetHandler(new LoginServer(server, networkManager));
			}
			
			break;
		case STATUS:
			networkManager.setConnectionState(ConnectionState.STATUS);
			networkManager.setNetHandler(new StatusServer(server, networkManager));
			break;
		default:
			throw new UnsupportedOperationException("Invalid intention " + packetIn.getRequestedState());
		}
		
	}
	
	@Override
	public void onDisconnection(String reason) {}
}
