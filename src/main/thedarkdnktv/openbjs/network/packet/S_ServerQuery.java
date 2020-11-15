package thedarkdnktv.openbjs.network.packet;

import java.io.IOException;

import thedarkdnktv.openbjs.api.network.Packet;
import thedarkdnktv.openbjs.api.util.PacketBuf;
import thedarkdnktv.openbjs.network.handlers.interfaces.IStatusClient;

/**
 * @author TheDarkDnKTv
 * TODO: Server info packet
 */
public class S_ServerQuery implements Packet<IStatusClient>  {
	
	private String message;
	
	public S_ServerQuery() {}
	
	public S_ServerQuery(int activeTables, int totalTables) {
		this.message = "Server is running successeffuly, " + activeTables + " / " + totalTables + " tables.";
	}
	
	@Override
	public void writePacketData(PacketBuf buf) throws IOException {
		buf.writeString(this.message);
	}

	@Override
	public void readPacketData(PacketBuf buf) throws IOException {
		this.message = buf.readString(Short.MAX_VALUE);
	}

	@Override
	public void processPacket(IStatusClient handler) {
		handler.handleServerQuery(this);
	}
}
