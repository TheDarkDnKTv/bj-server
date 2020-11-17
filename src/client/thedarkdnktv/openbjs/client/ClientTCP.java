package thedarkdnktv.openbjs.client;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thedarkdnktv.openbjs.api.API;
import thedarkdnktv.openbjs.api.annotation.Client;
import thedarkdnktv.openbjs.api.interfaces.IInitializable;
import thedarkdnktv.openbjs.api.interfaces.ITickable;
import thedarkdnktv.openbjs.api.network.base.ConnectionState;
import thedarkdnktv.openbjs.client.network.NetHandlerClient;
import thedarkdnktv.openbjs.network.handlers.interfaces.IStatusClient;
import thedarkdnktv.openbjs.network.packet.C_Handshake;
import thedarkdnktv.openbjs.network.packet.C_ServerQuery;
import thedarkdnktv.openbjs.network.packet.S_Pong;
import thedarkdnktv.openbjs.network.packet.S_ServerQuery;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
@Client(clientId = ClientTCP.ID, versionAPI = API.VERSION)
public class ClientTCP implements ITickable, IInitializable {
	
	public static final String ID = "TestClient";
	
	private static final Logger logger = LogManager.getLogger(ID);
	
	private NetHandlerClient handler;
	
	/**
	 *  When API detects @Client class,
	 *  it making instance of this class. <br>
	 *  So it is like init method.
	 */
	public ClientTCP() {}

	@Override
	public void update() {
		if (handler != null) {
			if (!handler.hasNoChannel() && handler.isChannelOpen()) {
				handler.processReceivedPackets();
			} else {
				logger.error("DEBUG DISCONNECT");
				handler.handleDisconnection();
				handler = null;
			}
		}
	}
	
	@Override
	public void init() {
		try {
			handler = NetHandlerClient.createAndConnect(InetAddress.getLocalHost(), 100);
			handler.setNetHandler(new IStatusClient() {
				
				
				@Override
				public void onDisconnection(String reason) {
					ClientTCP.logger.info("Disconnected: " + reason);
				}
				
				@Override
				public void handleServerQuery(S_ServerQuery packet) {
					ClientTCP.logger.info(packet.getMessage());
					ClientTCP.this.handler.closeChannel("Status recieved");
				}
				
				@Override
				public void handlePong(S_Pong packet) {
					ClientTCP.logger.info("PONG");
				}
			});
			
			handler.sendPacket(new C_Handshake(InetAddress.getLocalHost().getHostAddress(), 100, ConnectionState.STATUS));
			handler.sendPacket(new C_ServerQuery());
		} catch (Throwable e) {
			logger.catching(e);
		}
		
		logger.info("TCP Client successfully initialized");
	}
}