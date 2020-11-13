package thedarkdnktv.openbjs.client;

import thedarkdnktv.openbjs.api.annotation.Client;
import thedarkdnktv.openbjs.api.network.IActionHandler;
import thedarkdnktv.openbjs.api.network.IClient;
import thedarkdnktv.openbjs.api.network.Packet;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
@Client(clientId = SimpleClient.ID)
public class SimpleClient implements IClient {
	public static final String ID = "TestClient";
	
	public SimpleClient() {
	}

	
	@Override
	public void tick() {
		// TODO
	}
	
	@Override
	public void handle(Packet<?> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processConnection(IActionHandler handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnection(String reason) {
		// TODO Auto-generated method stub
		
	}
}