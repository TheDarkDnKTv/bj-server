package thedarkdnktv.openbjs.client;

import thedarkdnktv.openbjs.api.IClient;
import thedarkdnktv.openbjs.api.annotation.Client;

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
	public void update() {
		// TODO Auto-generated method stub
		
	}
}