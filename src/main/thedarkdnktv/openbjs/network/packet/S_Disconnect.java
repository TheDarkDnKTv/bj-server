package thedarkdnktv.openbjs.network.packet;

import java.io.IOException;

import thedarkdnktv.openbjs.api.network.Packet;
import thedarkdnktv.openbjs.network.handlers.interfaces.ILoginClient;
import thedarkdnktv.openbjs.util.PacketBuf;

/**
 * @author TheDarkDnKTv
 *
 */
public class S_Disconnect implements Packet<ILoginClient> {
	
	private String reason;
	
	public S_Disconnect() {}
	
	public S_Disconnect(String reason) {
		this.reason = reason;
	}
	
	@Override
	public void writePacketData(PacketBuf buf) throws IOException {
		buf.writeString(this.reason);
	}

	@Override
	public void readPacketData(PacketBuf buf) throws IOException {
		this.reason = buf.readString(Short.MAX_VALUE);
	}

	@Override
	public void processPacket(ILoginClient handler) {
		handler.handleDisconnect(this);
	}
}
