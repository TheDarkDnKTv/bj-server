package thedarkdnktv.openbjs.api.network.base;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;

import thedarkdnktv.openbjs.api.network.Packet;

/**
 * @author TheDarkDnKTv
 *
 */
public enum ConnectionState {
	HANDSHAKING(-1),
	STATUS(0),
	LOGIN(1),
	PLAY(2);
	
	private static final ConnectionState[] STATES_BY_ID;
	private static final Map<Class<? extends Packet<?>>, ConnectionState> STATES_BY_CLASS;
	
	private final int id;
	private final Map<PacketDirection, Map<Integer, Class<? extends Packet<?>>>> directionMaps;
	
	private ConnectionState(int protocolId) {
		this.directionMaps = new EnumMap<PacketDirection, Map<Integer, Class<? extends Packet<?>>>>(PacketDirection.class);
		this.id = protocolId;
	}
	
	static {
		STATES_BY_ID = new ConnectionState[4];
		STATES_BY_CLASS = new HashMap<>();
	}
	
	public Integer getPacketId(PacketDirection dir, Packet<?> packetIn) throws Exception {
		Map<Integer, Class<? extends Packet<?>>> map = this.directionMaps.get(dir);
		Entry<Integer, Class<? extends Packet<?>>> entry = map.entrySet().stream().filter(e -> e.getValue() == packetIn.getClass()).findFirst().get();
		return entry.getKey();
	}
	
	/**
	 * Get a packet from direction & id
	 * @apiNote return could be null
	 */
	public Packet<?> getPacket(PacketDirection dir, int packetId) throws InstantiationException, IllegalAccessException {
		Class<? extends Packet<?>> pClass = this.directionMaps.get(dir).get(new Integer(packetId));
		return pClass == null ? null : pClass.newInstance();
	}
	
	public int getId() {
		return id;
	}
	
	public static ConnectionState getFromPacket(Packet<?> packetIn) {
		return STATES_BY_CLASS.get(packetIn.getClass());
	}
	
	public static ConnectionState getById(int stateId) {
		return STATES_BY_ID[stateId - -1];
	}
	
	public <P extends Packet<?>> ConnectionState registerPacket(PacketDirection dir, Class<P> packetClass) {
		Map<Integer, Class<? extends Packet<?>>> map = this.directionMaps.get(dir);
		
		if (map == null) {
			map = new HashMap<>();
			this.directionMaps.put(dir, map);
		}
		
		if (map.containsValue(packetClass)) {
			Integer id = map.entrySet().stream().filter(entry -> entry.getValue() == packetClass).findFirst().get().getKey();
			String err = dir + " packet " + packetClass + " is already known to ID " + id;
			LogManager.getLogger().fatal(err);
			throw new IllegalArgumentException(err);
		} else {
			map.put(Integer.valueOf(map.size()), packetClass);
			return this;
		}
	}
	
	public static void registerPackets() {
		for (ConnectionState state : values()) { // HANDSHAKE, LOGIN, STATUS, PLAY
			int id = state.getId();
			
			STATES_BY_ID[id - -1] = state;
			for (PacketDirection dir : state.directionMaps.keySet()) { // SERVERBOUND, CLIENTBOUND
				for (Class<? extends Packet<?>> pClass : state.directionMaps.get(dir).values()) {
					if (STATES_BY_CLASS.containsKey(pClass) && STATES_BY_CLASS.get(pClass) != state) {
						throw new Error("Packet " + pClass + " is already assigned to protocol " + STATES_BY_CLASS.get(pClass));
					}
					
					try {
						pClass.newInstance();
					} catch (Throwable e) {
						throw new Error("Packet" + pClass + " failed instatiation check", e);
					}
					
					STATES_BY_CLASS.put(pClass, state);
				}
			}
			
			
		}
	}
}
