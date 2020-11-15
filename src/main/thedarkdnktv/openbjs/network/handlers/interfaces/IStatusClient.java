package thedarkdnktv.openbjs.network.handlers.interfaces;

import thedarkdnktv.openbjs.api.network.INetHandler;
import thedarkdnktv.openbjs.network.packet.S_Pong;
import thedarkdnktv.openbjs.network.packet.S_ServerQuery;

/**
 * @author TheDarkDnKTv
 *
 */
public interface IStatusClient extends INetHandler {
	void handleServerQuery(S_ServerQuery packet);
	
	void handlePong(S_Pong packet);
}
