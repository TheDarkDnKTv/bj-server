package thedarkdnktv.openbjs.command;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thedarkdnktv.openbjs.OpenBJS;
import thedarkdnktv.openbjs.game.Table;

/**
 * @author TheDarkDnKTv
 *
 */
public class CommandTable implements ICommand {
	private static final Logger logger = LogManager.getLogger();
	
	@Override
	public String[] getNames() {
		return new String[] {"table"};
	}

	@Override
	public boolean isValidInput(List<String> properties) {
		if (!properties.isEmpty()) {
			try {
				return null != SubCommand.valueOf(properties.remove(0).toUpperCase());
			} catch (Throwable e) {}
		}
		
		return false;
	}

	@Override
	public String getUsageString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Next sub commands available:\n");
		
		for (SubCommand com : SubCommand.values()) {
			buf.append(String.format(" * %-8s %s\n", com.name().toLowerCase(), com.description));
		}
		
		return buf.toString();
	}

	@Override
	public void execute(List<String> properties) {
		SubCommand com = SubCommand.valueOf(properties.remove(0).toUpperCase());
		switch (com) {
		case NEW:
			this.newTable(properties);
			break;
		case REMOVE:
			this.removeTable(properties);
			break;
		case LAUNCH:
			this.launch(properties);
			break;
		case STOP:
			this.stop(properties);
			break;
		case STATUS:
			this.status(properties);
			break;
		}
	}
	
	/*
	 * Sub Executors
	 */
	
	private void newTable(List<String> lines) {
		int boxes = Integer.parseInt(lines.get(0));
		int id = OpenBJS.INSTANCE.getTableManager().launchTable(new Table(boxes));
		logger.info("New table successfully created with id " + id);
	}
	
	private void removeTable(List<String> lines) {
		int id = Integer.parseInt(lines.get(0));
		if (OpenBJS.INSTANCE.getTableManager().removeTable(id)) {
			logger.info("Table was removed successfully");
		} else {
			logger.info("There is no table with id '" + id + "'");
		}
	}
	
	private void launch(List<String> lines) {
		int id = Integer.parseInt(lines.get(0));
		Table table = OpenBJS.INSTANCE.getTableManager().getTable(id);
		if (table != null) {
			table.launch();
			logger.info("Table with id " + id + " launched successfully");
		}
	}
	
	private void stop(List<String> lines) {
		// TODO
	}
	
	private void status(List<String> lines) {
		int id = Integer.parseInt(lines.get(0));
		Table table = OpenBJS.INSTANCE.getTableManager().getTable(id);
		logger.info(table.getLobbyName() + " " + table.getState()); // TODO
	}
	
	/**
	 * 
	 * @author TheDarkDnKTv
	 *
	 */
	private static enum SubCommand {
		NEW		("<boxes count[3-9]> creates new table on server"),
		REMOVE	("<id> removes a table from server"),
		LAUNCH	("<id> marks table as active"),
		STOP	("<id> stop games on table"),
		STATUS	("<id> show status of table");
		
		public String description;
		
		SubCommand(String desc) {
			this.description = desc;
		}
	}
}
