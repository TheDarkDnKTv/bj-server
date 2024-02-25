package thedarkdnktv.bjclient;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thedarkdnktv.openbjs.api.API;
import thedarkdnktv.openbjs.api.annotation.Client;
import thedarkdnktv.openbjs.api.interfaces.IInitializable;
import thedarkdnktv.openbjs.api.interfaces.ITickable;
import thedarkdnktv.openbjs.api.network.base.ConnectionState;
import thedarkdnktv.bjclient.network.NetHandlerClient;
import thedarkdnktv.openbjs.network.handlers.interfaces.IStatusClient;
import thedarkdnktv.openbjs.network.packet.C_Handshake;
import thedarkdnktv.openbjs.network.packet.C_LoginStart;
import thedarkdnktv.openbjs.network.packet.C_Ping;
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
			if (handler.isChannelOpen()) {
				handler.processReceivedPackets();
			} else {
				handler.handleDisconnection();
				handler = null;
			}
		}
	}
	
	@Override
	public void init() {
		try {
			handler = NetHandlerClient.createAndConnect(InetAddress.getLoopbackAddress(), 32760);
			handler.setNetHandler(new IStatusClient() {
				long pingSentAt = 0;
				
				@Override
				public void onDisconnection(String reason) {
				}
				
				@Override
				public void handleServerQuery(S_ServerQuery packet) {
					ClientTCP.logger.info(packet.getMessage());
					pingSentAt = System.currentTimeMillis();
					handler.sendPacket(new C_Ping(pingSentAt));
				}
				
				@Override
				public void handlePong(S_Pong packet) {
					long time = System.currentTimeMillis();
					ClientTCP.logger.info("PONG for " + ((long) time - pingSentAt) + "ms");
					handler.closeChannel("Finished");
				}
			});
			
			handler.sendPacket(new C_Handshake(InetAddress.getLocalHost().getHostAddress(), 100, ConnectionState.LOGIN));
			handler.sendPacket(new C_LoginStart("ТестUTF8"));
		} catch (Throwable e) {
			logger.catching(e);
		}
		
		logger.info("TCP Client successfully initialized");
	}
}