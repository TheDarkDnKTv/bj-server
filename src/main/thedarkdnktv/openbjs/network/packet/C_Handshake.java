package thedarkdnktv.openbjs.network.packet;

import java.io.IOException;

import thedarkdnktv.openbjs.api.network.Packet;
import thedarkdnktv.openbjs.network.ConnectionState;
import thedarkdnktv.openbjs.network.handlers.INetHandlerHandshakeServer;
import thedarkdnktv.openbjs.util.PacketBuf;

/**
 * @author TheDarkDnKTv
 *
 */
public class C_Handshake implements Packet<INetHandlerHandshakeServer> {
	
	private int protocolVersion;
	private String ip;
	private int port;
	private ConnectionState requestedState;
	
	@Override
	public void writePacketData(PacketBuf buffer) throws IOException {
		buffer.writeVarInt(protocolVersion);
		buffer.writeString(ip);
		buffer.writeShort(port);
		buffer.writeVarInt(requestedState.getId());
	}

	@Override
	public void readPacketData(PacketBuf buffer) throws IOException {
		protocolVersion = buffer.readVarInt();
		ip = buffer.readString(0xFF);
		port = buffer.readUnsignedShort();
		requestedState = ConnectionState.getById(buffer.readVarInt());
	}

	@Override
	public void processPacket(INetHandlerHandshakeServer handler) {
		handler.processHandshake(this);
	}
	
	public ConnectionState getRequestedState() {
		return requestedState;
	}
	
	public int getProtocolVerion() {
		return protocolVersion;
	}
}
