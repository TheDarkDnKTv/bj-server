package thedarkdnktv.openbjs.api.network.code;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import thedarkdnktv.openbjs.api.network.NetworkHandler;
import thedarkdnktv.openbjs.api.network.Packet;
import thedarkdnktv.openbjs.api.network.base.ConnectionState;
import thedarkdnktv.openbjs.api.network.base.PacketDirection;
import thedarkdnktv.openbjs.api.util.PacketBuf;

/**
 * @author TheDarkDnKTv
 *
 */
public class PacketDecoder extends ByteToMessageDecoder {
	
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_RECEIVED").addParents(NetworkHandler.NETWORK_MARKER);
	
	private final PacketDirection direction;
	
	public PacketDecoder(PacketDirection dir) {
		this.direction = dir;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() != 0) {
			PacketBuf buf = new PacketBuf(in);
			int varInt = buf.readVarInt();
			ConnectionState state = ctx.channel().attr(NetworkHandler.PROTOCOL_ATTRIBUTE_KEY).get();
			LOGGER.debug(state);
			Packet<?> packet = state.getPacket(direction, varInt);
			
			if (packet == null) {
				throw new IOException("Bad packet id: " + varInt);
			} else {
				packet.readPacketData(buf);
				
				if (buf.readableBytes() > 0) {
					throw new IOException("Packet was bigger than expecting!");
				} else {
					out.add(packet);
					LOGGER.debug(RECEIVED_PACKET_MARKER, " IN: [{}:{}] {}", ctx.channel().attr(NetworkHandler.PROTOCOL_ATTRIBUTE_KEY).get(), new Integer(varInt), packet.getClass().getName());
				}
			}
		}
	}
}
