package thedarkdnktv.openbjs.network.handlers.interfaces;

import thedarkdnktv.openbjs.api.network.base.INetHandler;
import thedarkdnktv.openbjs.network.packet.C_Handshake;

/**
 * @author TheDarkDnKTv
 *
 */
public interface IHandshakeServer extends INetHandler {
	
	void processHandshake(C_Handshake packetIn);
}
