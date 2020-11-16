package thedarkdnktv.openbjs.api.network.code;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import thedarkdnktv.openbjs.api.network.NetworkHandler;
import thedarkdnktv.openbjs.api.network.Packet;
import thedarkdnktv.openbjs.api.network.base.ConnectionState;
import thedarkdnktv.openbjs.api.network.base.PacketDirection;
import thedarkdnktv.openbjs.api.util.PacketBuf;

/**
 * @author TheDarkDnKTv
 *
 */
public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_SENT").addParents(NetworkHandler.NETWORK_MARKER);
	private final PacketDirection direction;
	
	public PacketEncoder(PacketDirection dir) {
		this.direction = dir;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Packet<?> packet, ByteBuf buf) throws Exception {
		ConnectionState state = ctx.channel().attr(NetworkHandler.PROTOCOL_ATTRIBUTE_KEY).get();
		
		if (state == null) {
			throw new RuntimeException("ConnectionProtocol unknown: " + packet.toString());
		} else {
			Integer integer = state.getPacketId(this.direction, packet);
			LOGGER.debug(RECEIVED_PACKET_MARKER, "OUT: [{}:{}] {}", ctx.channel().attr(NetworkHandler.PROTOCOL_ATTRIBUTE_KEY).get(), integer, packet.getClass().getName());
			
			if (integer == null) {
				throw new IOException("Can not serialize unregistered packet");
			} else {
				PacketBuf pBuf = new PacketBuf(buf);
				pBuf.writeVarInt(integer.intValue());
				
				try {
					packet.writePacketData(pBuf);
				} catch (Throwable e) {
					LOGGER.error(e);
				}
			}
		}
	}
}
