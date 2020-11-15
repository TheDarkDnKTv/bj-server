package thedarkdnktv.openbjs.network.packet;

import java.io.IOException;

import thedarkdnktv.openbjs.api.network.Packet;
import thedarkdnktv.openbjs.network.handlers.interfaces.IStatusServer;
import thedarkdnktv.openbjs.util.PacketBuf;

/**
 * @author TheDarkDnKTv
 *
 */
public class C_ServerQuery implements Packet<IStatusServer> {

	@Override
	public void writePacketData(PacketBuf buf) throws IOException {}

	@Override
	public void readPacketData(PacketBuf buf) throws IOException {}

	@Override
	public void processPacket(IStatusServer handler) {
		handler.processServerQuery(this);
	}
}
