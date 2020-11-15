package thedarkdnktv.openbjs.network.handlers.interfaces;

import thedarkdnktv.openbjs.api.network.INetHandler;
import thedarkdnktv.openbjs.network.packet.C_Ping;
import thedarkdnktv.openbjs.network.packet.C_ServerQuery;

/**
 * @author TheDarkDnKTv
 *
 */
public interface IStatusServer extends INetHandler {
	void processPing(C_Ping packet);
	
	void processServerQuery(C_ServerQuery packet);
}
