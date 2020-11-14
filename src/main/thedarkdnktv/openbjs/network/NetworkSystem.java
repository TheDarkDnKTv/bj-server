package thedarkdnktv.openbjs.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import thedarkdnktv.openbjs.OpenBJS;
import thedarkdnktv.openbjs.manage.NetworkManager;
import thedarkdnktv.openbjs.util.ThreadFactoryBuilder;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class NetworkSystem {
	private static final Logger logger = LogManager.getLogger();
	
	public static final LazyLoadBase<NioEventLoopGroup> SERVER_NIO_EVENTLOOP;
	public static final LazyLoadBase<EpollEventLoopGroup> SERVER_EPOLL_EVENTLOOP;
	public static final LazyLoadBase<DefaultEventLoopGroup> SERVER_LOCAL_EVENTLOOP;
	public static final int PROTOCOL_VERSION = 100;
	
	private final OpenBJS server;
	private final List<ChannelFuture> endpoints = Collections.<ChannelFuture>synchronizedList(new ArrayList<ChannelFuture>());
	private final List<NetworkManager> networkManagers = Collections.<NetworkManager>synchronizedList(new ArrayList<NetworkManager>());
	
	public volatile boolean isAlive;
	
	public NetworkSystem(OpenBJS server) {
		this.server = Objects.requireNonNull(server);
		this.isAlive = true;
	}
	
	public void networkTick() {
		synchronized (this.networkManagers) {
//			for (NetworkManager manager : this.networkManagers) {
				// TODO process packets
//			}
		}
	}
	
	public void addEndpoint(InetAddress address, int port) throws IOException {
		synchronized (this.endpoints) {
			Class<? extends ServerSocketChannel> chClz;
			LazyLoadBase<? extends EventLoopGroup> lazyloadbase;
			
			if (Epoll.isAvailable()) { // TODO config option for this
				chClz = EpollServerSocketChannel.class;
				lazyloadbase = SERVER_EPOLL_EVENTLOOP;
				logger.info("Activated epoll cahnnel type");
			} else {
				chClz = NioServerSocketChannel.class;
				lazyloadbase = SERVER_NIO_EVENTLOOP;
				logger.info("Using default channel type");
			}
			
			ServerBootstrap bootstrap = new ServerBootstrap().channel(chClz).childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					try {
						ch.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
					} catch (ChannelException e) {}
					
					ch.pipeline().addLast("timeout", new ReadTimeoutHandler(30)); // TODO add a ping handler
					NetworkManager manager = new NetworkManager(PacketDirection.SERVERBOUND);
					NetworkSystem.this.networkManagers.add(manager);
					ch.pipeline().addLast("packet_handler", manager);
//					manager.setNetHandler(handler); // TODO handler
				}
			});
			
			this.endpoints.add(bootstrap.group(lazyloadbase.getValue()).localAddress(address, port).bind().syncUninterruptibly());
		}
	}
	
	public void terminateEndpoints() {
		this.isAlive = false;
		
		for (ChannelFuture channel : this.endpoints) {
			try {
				channel.channel().close().sync();
			} catch (InterruptedException e) {
				logger.error("Interrupted whilst closing channel");
			}
		}
	}
	
	static {
		SERVER_NIO_EVENTLOOP = new LazyLoadBase<NioEventLoopGroup>() {
			@Override
			protected NioEventLoopGroup load() {
				return new NioEventLoopGroup(0, new ThreadFactoryBuilder().setDeamon(true).setNameFormat("Netty Server IO #%d").build());
			}
		};
		SERVER_EPOLL_EVENTLOOP = new LazyLoadBase<EpollEventLoopGroup>() {
			@Override
			protected EpollEventLoopGroup load() {
				return new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setDeamon(true).setNameFormat("Netty Epoll Server IO #%d").build());
			}
		};
		SERVER_LOCAL_EVENTLOOP = new LazyLoadBase<DefaultEventLoopGroup>() {
			@Override
			protected DefaultEventLoopGroup load() {
				return new DefaultEventLoopGroup(0, new ThreadFactoryBuilder().setDeamon(true).setNameFormat("Netty Local Server IO #%d").build());
			}
		};
	}
}
