package thedarkdnktv.openbjs.network.packet;

import java.io.IOException;

import thedarkdnktv.openbjs.api.network.Packet;
import thedarkdnktv.openbjs.api.network.base.ConnectionState;
import thedarkdnktv.openbjs.api.util.PacketBuf;
import thedarkdnktv.openbjs.network.NetworkSystem;
import thedarkdnktv.openbjs.network.handlers.interfaces.IHandshakeServer;

/**
 * @author TheDarkDnKTv
 *
 */
public class C_Handshake implements Packet<IHandshakeServer> {
	
	private int protocolVersion;
	private String ip;
	private int port;
	private ConnectionState requestedState;
	
	public C_Handshake() {}

	public C_Handshake(String ip, int port, ConnectionState state) {
		this.ip = ip;
		this.port = port;
		this.requestedState = state;
		this.protocolVersion = NetworkSystem.PROTOCOL_VERSION;
	}
	
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
	public void processPacket(IHandshakeServer handler) {
		handler.processHandshake(this);
	}
	
	public ConnectionState getRequestedState() {
		return requestedState;
	}
	
	public int getProtocolVerion() {
		return protocolVersion;
	}
}
