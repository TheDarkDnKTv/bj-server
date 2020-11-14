package thedarkdnktv.openbjs.api.network;

import java.io.IOException;

import thedarkdnktv.openbjs.util.PacketBuf;

/**
 * @author TheDarkDnKTv
 *
 */
public interface Packet<T extends INetHandler> {
	public void writePacketData(PacketBuf buf) throws IOException;
	
	public void readPacketData(PacketBuf buf) throws IOException;
	
	public void processPacket(T handler);
}
