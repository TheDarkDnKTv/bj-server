package thedarkdnktv.openbjs.client;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import thedarkdnktv.openbjs.api.API;
import thedarkdnktv.openbjs.api.annotation.Client;
import thedarkdnktv.openbjs.api.interfaces.ITickable;
import thedarkdnktv.openbjs.api.network.base.LazyLoadBase;
import thedarkdnktv.openbjs.client.network.NetHandlerClient;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
@Client(clientId = ClientTCP.ID, versionAPI = API.VERSION)
public class ClientTCP implements ITickable {
	
	public static final String ID = "TestClient";
	
	private static final Logger logger = LogManager.getLogger(ID);
	
	private NetHandlerClient handler;
	
	/**
	 *  When API detects @Client class,
	 *  it making instance of this class. <br>
	 *  So it is like init method.
	 */
	public ClientTCP() {
		handler = new NetHandlerClient();
		
		
		try {
			handler = NetHandlerClient.createAndConnect(InetAddress.getLocalHost(), 100);
//			handler.sendPacket(packet);
		} catch (Throwable e) {
			logger.catching(e);
		}
		
		logger.info("TCP Client successfully initialized");
	}

	@Override
	public void update() {
		
		
	}
}