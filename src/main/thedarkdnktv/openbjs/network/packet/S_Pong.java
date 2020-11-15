package thedarkdnktv.openbjs.network.packet;

import java.io.IOException;

import thedarkdnktv.openbjs.api.network.Packet;
import thedarkdnktv.openbjs.api.util.PacketBuf;
import thedarkdnktv.openbjs.network.handlers.interfaces.IStatusClient;

public class S_Pong implements Packet<IStatusClient> {
	private long clientTime;
	
	public S_Pong() {}
	
	public S_Pong(long clientTime) {
		this.clientTime = clientTime;
	}

	@Override
	public void writePacketData(PacketBuf buf) throws IOException {
		this.clientTime = buf.readLong();
	}

	@Override
	public void readPacketData(PacketBuf buf) throws IOException {
		buf.writeLong(this.clientTime);
	}

	@Override
	public void processPacket(IStatusClient handler) {
		handler.handlePong(this);
	}
}
