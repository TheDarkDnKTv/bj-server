package thedarkdnktv.openbjs.api.interfaces;

import thedarkdnktv.openbjs.api.network.Packet;
import thedarkdnktv.openbjs.api.network.base.INetHandler;

/**
 * To implement a API-based client please use this interface with 
 * {@link thedarkdnktv.openbjs.api.annotation.Client} annotation to 
 * mark your client classes
 * 
 * @author TheDarkDnKTv
 * 
 */
public interface IClient extends INetHandler {
	
	public void tick();
	
	public void handle(Packet<?> data);
	
}
