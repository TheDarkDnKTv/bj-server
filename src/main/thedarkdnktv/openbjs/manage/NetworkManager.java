package thedarkdnktv.openbjs.manage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import thedarkdnktv.openbjs.api.network.INetHandler;
import thedarkdnktv.openbjs.api.network.Packet;

public class NetworkManager extends SimpleChannelInboundHandler<Packet<?>> {
	
	private INetHandler handler;
	
	public NetworkManager() {
		
	
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void setNetHandler(INetHandler handler) {
		this.handler = handler;
	}
}
