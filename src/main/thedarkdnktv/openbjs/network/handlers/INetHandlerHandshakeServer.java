package thedarkdnktv.openbjs.network.handlers;

import thedarkdnktv.openbjs.api.network.INetHandler;
import thedarkdnktv.openbjs.network.packet.C_Handshake;

/**
 * @author TheDarkDnKTv
 *
 */
public interface INetHandlerHandshakeServer extends INetHandler {
	
	void processHandshake(C_Handshake packetIn);
}
