package thedarkdnktv.openbjs.network.handlers;

import thedarkdnktv.openbjs.OpenBJS;
import thedarkdnktv.openbjs.manage.NetworkManager;
import thedarkdnktv.openbjs.network.ConnectionState;
import thedarkdnktv.openbjs.network.NetworkSystem;
import thedarkdnktv.openbjs.network.packet.C_Handshake;

/**
 * @author TheDarkDnKTv
 *
 */
public class HandshakeTCP implements INetHandlerHandshakeServer {
	private final OpenBJS server;
	private final NetworkManager networkManager;
	
	public HandshakeTCP(OpenBJS serverIn, NetworkManager netManager) {
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
//				networkManager.sendPacket(packet);
				networkManager.closeChannel(err);
			} else if (packetIn.getProtocolVerion() > NetworkSystem.PROTOCOL_VERSION) {
				String err = "Outdated server version";
//				networkManager.sendPacket(packet); // TODO disconnect packet
				networkManager.closeChannel(err);
			} else {
//				networkManager.setNetHandler(handler); // login handler
			}
			
			break;
		case STATUS:
			networkManager.setConnectionState(ConnectionState.STATUS);
//			networkManager.setNetHandler(handler); // ServerStatus handler
			break;
		default:
			throw new UnsupportedOperationException("Invalid intention " + packetIn.getRequestedState());
		}
		
	}
	
	@Override
	public void onDisconnection(String reason) {}
}
