package thedarkdnktv.openbjs.command;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thedarkdnktv.openbjs.OpenBJS;

/**
 * @author TheDarkDnKTv
 *
 */
public class CommandStatus implements ICommand {
	private static final Logger log = LogManager.getLogger();
	
	
	@Override
	public String[] getNames() {
		return new String[] {"info", "status"};
	}

	@Override
	public boolean isValidInput(List<String> properties) {
		return true;
	}

	@Override
	public String getUsageString() {
		return "";
	}

	@Override
	public void execute(List<String> properties) {
		int total = OpenBJS.INSTANCE.getTableManager().totalTablesCount();
		int active = OpenBJS.INSTANCE.getTableManager().activeTableCount();
		log.info("Running total " + total + " tables, active is " + active + " tables");
	}
}
