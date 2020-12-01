package thedarkdnktv.openbjs.network.packet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import thedarkdnktv.openbjs.api.network.Packet;
import thedarkdnktv.openbjs.api.util.PacketBuf;
import thedarkdnktv.openbjs.network.handlers.interfaces.ILoginServer;

public class C_LoginStart implements Packet<ILoginServer> {
	
	private String login;
	
	public C_LoginStart() {}
	
	public C_LoginStart(String name) {
		login = name;
	}
	
	@Override
	public void writePacketData(PacketBuf buf) throws IOException {
		buf.writeShort(login.length());
		buf.writeCharSequence(login, StandardCharsets.UTF_8);
	}

	@Override
	public void readPacketData(PacketBuf buf) throws IOException {
		short len = buf.readShort();
		login = buf.readCharSequence(len, StandardCharsets.UTF_8).toString();
	}

	@Override
	public void processPacket(ILoginServer handler) {
		handler.processLoginStart(this);
	}
	
	public String getLogin() {
		return login;
	}
}
