package thedarkdnktv.openbjs.network.packet;

import java.io.IOException;

import thedarkdnktv.openbjs.api.network.Packet;
import thedarkdnktv.openbjs.api.util.PacketBuf;
import thedarkdnktv.openbjs.network.handlers.interfaces.IStatusServer;

/**
 * @author TheDarkDnKTv
 *
 */
public class C_Ping implements Packet<IStatusServer> {
	
	private long clientTime;
	
	public C_Ping() {
	}
	
	public C_Ping(long time) {
		clientTime = time;
	}
	
	@Override
	public void writePacketData(PacketBuf buf) throws IOException {
		buf.writeLong(this.clientTime);
	}

	@Override
	public void readPacketData(PacketBuf buf) throws IOException {
		this.clientTime = buf.readLong();
	}

	@Override
	public void processPacket(IStatusServer handler) {
		handler.processPing(this);
	}
	
	public long getClientTime() {
		return this.clientTime;
	}
}
