package thedarkdnktv.openbjs.network.handlers.interfaces;

import thedarkdnktv.openbjs.api.network.base.INetHandler;
import thedarkdnktv.openbjs.network.packet.C_LoginStart;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public interface ILoginServer extends INetHandler {
	void processLoginStart(C_LoginStart packet);
	
	// void processEncryptionResponse() // TODO encryption
}
