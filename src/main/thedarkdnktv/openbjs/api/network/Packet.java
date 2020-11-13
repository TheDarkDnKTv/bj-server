package thedarkdnktv.openbjs.api.network;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

/**
 * @author TheDarkDnKTv
 *
 */
public interface Packet<T extends INetHandler> {
	public void writePacketData(ByteBuf buffer) throws IOException;
	
	public void readPacketData(ByteBuf buffer) throws IOException;
	
	public void processPacket(T handler);
}
