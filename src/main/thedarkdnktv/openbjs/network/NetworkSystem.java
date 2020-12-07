package thedarkdnktv.openbjs.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import thedarkdnktv.openbjs.OpenBJS;
import thedarkdnktv.openbjs.api.network.NetworkHandler;
import thedarkdnktv.openbjs.api.network.base.ConnectionState;
import thedarkdnktv.openbjs.api.network.base.LazyLoadBase;
import thedarkdnktv.openbjs.api.network.base.PacketDirection;
import thedarkdnktv.openbjs.api.network.codec.PacketDecoder;
import thedarkdnktv.openbjs.api.network.codec.PacketEncoder;
import thedarkdnktv.openbjs.api.network.codec.VarIntFrameDecoder;
import thedarkdnktv.openbjs.api.network.codec.VarIntFrameEncoder;
import thedarkdnktv.openbjs.api.util.ThreadFactoryBuilder;
import thedarkdnktv.openbjs.network.handlers.HandshakeTCP;
import thedarkdnktv.openbjs.network.packet.C_Handshake;
import thedarkdnktv.openbjs.network.packet.C_LoginStart;
import thedarkdnktv.openbjs.network.packet.C_Ping;
import thedarkdnktv.openbjs.network.packet.C_ServerQuery;
import thedarkdnktv.openbjs.network.packet.S_Disconnect;
import thedarkdnktv.openbjs.network.packet.S_Pong;
import thedarkdnktv.openbjs.network.packet.S_ServerQuery;

import static thedarkdnktv.openbjs.api.network.base.ConnectionState.*;

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
	private final List<NetworkHandler> networkManagers = Collections.<NetworkHandler>synchronizedList(new ArrayList<NetworkHandler>());
	
	public volatile boolean isAlive;
	
	public NetworkSystem(OpenBJS server) {
		this.server = Objects.requireNonNull(server);
		this.isAlive = true;
	}
	
	public void networkTick() {
		synchronized (this.networkManagers) {
			Iterator<NetworkHandler> iter = this.networkManagers.iterator();
			
			while (iter.hasNext()) {
				final NetworkHandler manager = iter.next();
				if (!manager.hasNoChannel() && manager.isChannelOpen()) {
					try {
						manager.processReceivedPackets();
					} catch (Throwable e) {
						logger.warn("Failed to handle packet for {}", manager.getRemoteAddress(), e);
						manager.sendPacket(new S_Disconnect("Internal server error"), new GenericFutureListener<Future<? super Void>>() {
							@Override
							public void operationComplete(Future<? super Void> future) throws Exception {
								manager.closeChannel("Internal server error");
							}
						});
						manager.disableAutoRead();
					}
				} else {
					iter.remove();
					manager.handleDisconnection();
				}
			}
		}
	}
	
	public void addEndpoint(InetAddress address, int port) throws IOException {
		synchronized (this.endpoints) {
			Class<? extends ServerSocketChannel> chClz;
			LazyLoadBase<? extends EventLoopGroup> lazyloadbase;
			
			if (server.getServerConfig().isUsingEpoll() && Epoll.isAvailable()) {
				chClz = EpollServerSocketChannel.class;
				lazyloadbase = SERVER_EPOLL_EVENTLOOP;
				logger.info(NetworkHandler.NETWORK_MARKER, "Activated epoll cahnnel type");
			} else {
				chClz = NioServerSocketChannel.class;
				lazyloadbase = SERVER_NIO_EVENTLOOP;
				logger.info(NetworkHandler.NETWORK_MARKER, "Using default channel type");
			}
			
			ServerBootstrap bootstrap = new ServerBootstrap().channel(chClz).childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					try {
						ch.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
					} catch (ChannelException e) {}
					
					ch.pipeline()
						.addLast("timeout", new ReadTimeoutHandler(30))
						.addLast("splitter", new VarIntFrameDecoder())
						.addLast("decoder", new PacketDecoder(PacketDirection.SERVERBOUND))
						.addLast("prepender", new VarIntFrameEncoder())
						.addLast("encoder", new PacketEncoder(PacketDirection.CLIENTBOUND));
					NetworkHandler manager = new NetworkHandler(PacketDirection.SERVERBOUND);
					NetworkSystem.this.networkManagers.add(manager);
					ch.pipeline().addLast("packet_handler", manager);
					manager.setNetHandler(new HandshakeTCP(NetworkSystem.this.server, manager));
					
					NetworkSystem.logger.debug(NetworkHandler.NETWORK_MARKER, "Connection from " + ch.remoteAddress().toString());
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
		
		HANDSHAKING	.registerPacket(PacketDirection.SERVERBOUND, C_Handshake.class);
		
		STATUS		.registerPacket(PacketDirection.SERVERBOUND, C_Ping.class);
		STATUS		.registerPacket(PacketDirection.CLIENTBOUND, S_Pong.class);
		STATUS		.registerPacket(PacketDirection.SERVERBOUND, C_ServerQuery.class);
		STATUS		.registerPacket(PacketDirection.CLIENTBOUND, S_ServerQuery.class);
		
		LOGIN		.registerPacket(PacketDirection.CLIENTBOUND, S_Disconnect.class);
		LOGIN		.registerPacket(PacketDirection.SERVERBOUND, C_LoginStart.class);
		
		ConnectionState.registerPackets();
	}
}
