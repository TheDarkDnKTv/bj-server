package thedarkdnktv.openbjs.network.handlers.interfaces;

import thedarkdnktv.openbjs.api.network.INetHandler;
import thedarkdnktv.openbjs.network.packet.S_Disconnect;

/**
 * @author TheDarkDnKTv
 *
 */
public interface ILoginClient extends INetHandler {
	void handleLoginSuccess(); // TODO packet login success
	
	void handleDisconnect(S_Disconnect packet);
	
	//handleEncryptionRequest // TODO
	
	//handleEnableCompression
}
