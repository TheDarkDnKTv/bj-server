package thedarkdnktv.openbjs.manage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import thedarkdnktv.openbjs.game.Table;
import thedarkdnktv.openbjs.game.Table.State;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class TableManager {
	private Map<Integer, Table> gamingTables;
	
	public TableManager() {
		gamingTables = new HashMap<>();
		// TODO load from config later
	}
	
	public int launchTable(Table table) {
		Objects.requireNonNull(table);
		Entry<Integer, Table> entry = gamingTables.entrySet().stream()
				.filter(e -> e.getValue() == null)
				.findFirst()
				.orElse(null);
		int id = entry == null ? gamingTables.size() : entry.getKey().intValue();
		table.setID(id);
		gamingTables.put(id, table);
		
		return id;
	}
	
	public void updateTables() {
		for (Table table : gamingTables.values()) {
			table.update();
		}
	}
	
	public boolean hasTables() {
		return !gamingTables.isEmpty();
	}
	
	public int activeTableCount() {
		return (int) gamingTables.values().stream().filter(t -> t.getState() != State.DISABLED).count();
	}
	
	public int totalTablesCount() {
		return gamingTables.size();
	}
	
	public Table getTable(int id) {
		return gamingTables.get(Integer.valueOf(id));
	}
	
	public boolean removeTable(int id) {
		Table table = gamingTables.remove(Integer.valueOf(id));
		return table != null;
	}
}
