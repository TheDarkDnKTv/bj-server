package thedarkdnktv.openbjs.client.network;

import java.net.InetAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import thedarkdnktv.openbjs.api.network.NetworkHandler;
import thedarkdnktv.openbjs.api.network.base.LazyLoadBase;
import thedarkdnktv.openbjs.api.network.base.PacketDirection;
import thedarkdnktv.openbjs.api.network.codec.PacketDecoder;
import thedarkdnktv.openbjs.api.network.codec.PacketEncoder;
import thedarkdnktv.openbjs.api.network.codec.VarIntFrameDecoder;
import thedarkdnktv.openbjs.api.network.codec.VarIntFrameEncoder;
import thedarkdnktv.openbjs.api.util.ThreadFactoryBuilder;

/**
 * @author TheDarkDnKTv
 *
 */
public class NetHandlerClient extends NetworkHandler {
	
	public static final LazyLoadBase<NioEventLoopGroup> CLIENT_NIO_EVENTLOOP;
	public static final LazyLoadBase<EpollEventLoopGroup> CLIENT_EPOLL_EVENTLOOP;
	public static final LazyLoadBase<DefaultEventLoopGroup> CLIENT_LOCAL_EVENTLOOP;
	
	public NetHandlerClient() {
		super(PacketDirection.CLIENTBOUND);
	}

	public static NetHandlerClient createAndConnect(InetAddress address, int port) throws Throwable {
		NetHandlerClient manager = new NetHandlerClient();
		LazyLoadBase<? extends EventLoopGroup> loadBase;
		Class<? extends SocketChannel> channel;
		
		if (Epoll.isAvailable()) { // TODO EPOl config
			loadBase = NetHandlerClient.CLIENT_EPOLL_EVENTLOOP;
			channel = EpollSocketChannel.class;
		} else {
			loadBase = NetHandlerClient.CLIENT_NIO_EVENTLOOP;
			channel = NioSocketChannel.class;
		}
		
		Bootstrap boot = new Bootstrap().channel(channel).group(loadBase.getValue()).handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				try {
					ch.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
				} catch (ChannelException e) {}
				
				ch.pipeline()
					.addLast("timeout", new ReadTimeoutHandler(30))
					.addLast("splitter", new VarIntFrameDecoder())
					.addLast("decoder", new PacketDecoder(PacketDirection.CLIENTBOUND))
					.addLast("pretender", new VarIntFrameEncoder())
					.addLast("encoder", new PacketEncoder(PacketDirection.SERVERBOUND))
					.addLast("packet_handler", manager);
			}
		});
		
		boot.connect(address, port).syncUninterruptibly();
		
		return manager;
	}
	
	static {
		CLIENT_NIO_EVENTLOOP = new LazyLoadBase<NioEventLoopGroup>() {
			@Override
			protected NioEventLoopGroup load() {
				return new NioEventLoopGroup(0, new ThreadFactoryBuilder().setDeamon(true).setNameFormat("Netty Client IO #%d").build());
			}
		};
		CLIENT_EPOLL_EVENTLOOP = new LazyLoadBase<EpollEventLoopGroup>() {
			@Override
			protected EpollEventLoopGroup load() {
				return new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setDeamon(true).setNameFormat("Netty Epoll Client IO #%d").build());
			}
		};
		CLIENT_LOCAL_EVENTLOOP = new LazyLoadBase<DefaultEventLoopGroup>() {
			@Override
			protected DefaultEventLoopGroup load() {
				return new DefaultEventLoopGroup(0, new ThreadFactoryBuilder().setDeamon(true).setNameFormat("Netty Local Client IO #%d").build());
			}
		};
	}
}
