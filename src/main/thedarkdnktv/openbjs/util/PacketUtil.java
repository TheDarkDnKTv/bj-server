package thedarkdnktv.openbjs.util;

import thedarkdnktv.openbjs.api.network.Packet;
import thedarkdnktv.openbjs.api.network.base.INetHandler;

/**
 * @author TheDarkDnKTv
 *
 */
public class PacketUtil {
	public static <H extends INetHandler> void enqueue(Packet<H> packet, H handler, IThreadListener scheduler) {
		if (!scheduler.calledFromProperThread()) {
			scheduler.scheduleTask(() -> {
				packet.processPacket(handler);
			});
		}
	}
}
