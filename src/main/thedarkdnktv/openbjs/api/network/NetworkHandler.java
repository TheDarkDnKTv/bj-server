package thedarkdnktv.openbjs.api.network;

import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.sun.corba.se.impl.orbutil.threadpool.TimeoutException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import thedarkdnktv.openbjs.api.interfaces.ITickable;
import thedarkdnktv.openbjs.api.network.base.ConnectionState;
import thedarkdnktv.openbjs.api.network.base.INetHandler;
import thedarkdnktv.openbjs.api.network.base.PacketDirection;

public class NetworkHandler extends SimpleChannelInboundHandler<Packet<?>> {
	
	protected static final Logger logger = LogManager.getLogger();
	public static final Marker NETWORK_MARKER = MarkerManager.getMarker("NETWORK");
	public static final AttributeKey<ConnectionState> PROTOCOL_ATTRIBUTE_KEY = AttributeKey.<ConnectionState>valueOf("protocol");
	
	@SuppressWarnings("unused")
	private final PacketDirection direction;
	private final Queue<PacketEntry> outboundPacketsQueue = new ConcurrentLinkedQueue<>();
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	
	protected INetHandler handler;
	protected Channel channel;
	protected SocketAddress socketAddress;
	
	protected String terminationReason;
	protected boolean disconnected;
	
	public NetworkHandler(PacketDirection dir) {
		this.disconnected = false;
		this.direction = dir;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) throws Exception {
		if (this.channel.isOpen()) {
			try {
				((Packet<INetHandler>) msg).processPacket(this.handler);
			} catch (Throwable e) {
				logger.catching(Level.DEBUG, e);
			}
		}
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		this.channel = ctx.channel();
		this.socketAddress = this.channel.remoteAddress();
		
		try {
			
		} catch (Throwable e) {
			logger.fatal(e);
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		this.closeChannel("End of stream");
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable reason) throws Exception {
		String msg;
		if (reason instanceof TimeoutException) {
			msg = "Time out";
		} else {
			msg = "Exception occured: " + reason;
		}
		
		logger.debug(msg, reason);
		this.closeChannel(msg);
	}
	
	public void sendPacket(Packet<?> packet) {
		this.sendPacket(packet, (GenericFutureListener<? extends Future<? super Void>>)null);
	}
	
	@SafeVarargs
	public final void sendPacket(Packet<?> packetIn, GenericFutureListener<? extends Future <? super Void >> ... listeners) {
		if (isChannelOpen()) {
			this.flushOutboundQueue();
			this.dispatchPacket(packetIn, listeners);
		} else {
			this.readWriteLock.writeLock().lock();
			try {
				this.outboundPacketsQueue.add(new PacketEntry(packetIn, listeners));
			} finally {
				this.readWriteLock.writeLock().unlock();
			}
		}
	}
	
	protected void flushOutboundQueue() {
		if (isChannelOpen()) {
			this.readWriteLock.readLock().lock();
			try {
				while (!this.outboundPacketsQueue.isEmpty()) {
					PacketEntry entry = this.outboundPacketsQueue.poll();
					this.dispatchPacket(entry.packet, entry.futureListeners);
				}
			} finally {
				this.readWriteLock.readLock().unlock();
			}
		}
	}
	
	protected void dispatchPacket(final Packet<?> packet, final GenericFutureListener<? extends Future<? super Void>>[] futureListeners) {
		ConnectionState state = ConnectionState.getFromPacket(packet);
		ConnectionState state1 = this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).get();
		
		if (state != state1) {
			logger.debug("Disabled auto read");
			this.channel.config().setAutoRead(false);
		}
		
		if (this.channel.eventLoop().inEventLoop()) {
			if (state != state1) {
				this.setConnectionState(state);
			}
			
			ChannelFuture chFuture = this.channel.writeAndFlush(packet);
			
			if (chFuture != null) {
				chFuture.addListeners(futureListeners);
			}
			
			chFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
		} else {
			this.channel.eventLoop().execute(new Runnable() {
				@Override
				public void run() {
					if (state != state1) {
						NetworkHandler.this.setConnectionState(state);
						
						ChannelFuture chFuture = NetworkHandler.this.channel.writeAndFlush(packet);
						
						if (chFuture != null) {
							chFuture.addListeners(futureListeners);
						}
						
						chFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
					}
				}
			});
		}
	}
	
	public void processReceivedPackets() {
		this.flushOutboundQueue();
		if (this.handler instanceof ITickable) {
			((ITickable)this.handler).update();
		}
		
		if (this.channel != null) {
			this.channel.flush();
		}
	}
	
	public void setConnectionState(ConnectionState newState) {
		this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).set(newState);
		this.channel.config().setAutoRead(true);
		logger.debug("Enabled auto read");
	}
	
	public SocketAddress getRemoteAddress() {
		return this.socketAddress;
	}
	
	public void closeChannel(String message) {
		if (this.isChannelOpen()) {
			this.channel.close().awaitUninterruptibly();
			this.terminationReason = message;
		}
	}
	
	public boolean isChannelOpen() {
		return this.channel != null && this.channel.isOpen();
	}
	
	public boolean hasNoChannel() {
		return this.channel == null;
	}
	
	public void setNetHandler(INetHandler handler) {
		this.handler = handler;
	}
	
	public INetHandler getNetHandler() {
		return this.handler;
	}
	
	public String getExitMessage() {
		return this.terminationReason;
	}
	
	public void disableAutoRead() {
		this.channel.config().setAutoRead(false);
	}
	
	public void handleDisconnection() {
		if (isChannelOpen()) {
			if (this.disconnected) {
				logger.warn("handleDisconnection() called twice");
			} else {
				this.disconnected = true;
				
				this.getNetHandler().onDisconnection(this.terminationReason != null ? this.terminationReason : "Disconnected");
			}
		}
	}
	
	protected static class PacketEntry {
		public final Packet<?> packet;
		public final GenericFutureListener<? extends Future<? super Void>>[] futureListeners;
		
		@SafeVarargs
		public PacketEntry(Packet<?> inPacket, GenericFutureListener<? extends Future<? super Void >>...inFutureListeners) {
			this.packet = inPacket;
			this.futureListeners = inFutureListeners;
		}
	}
}
