package thedarkdnktv.openbjs.command;

import java.util.List;
import java.util.function.Function;

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
			SubCommand com = SubCommand.valueOf(properties.remove(0).toUpperCase());
			return com != null && com.checkArgs.apply(properties);
		}
		
		return false;
	}

	@Override
	public String getUsageString() {
		return "Next arguments is available for this command:\n"
				+ " * new <amount of boxes>\n"
				+ " * stop <table id>";
	}

	@Override
	public void execute(List<String> properties) {
		SubCommand com = SubCommand.valueOf(properties.remove(0).toUpperCase());
		switch (com) {
		case NEW:
			this.newTable(Integer.parseInt(properties.get(0)));
			break;
		case STOP:
			this.removeTable(Integer.parseInt(properties.get(0)));
			break;
		}
	}
	
	private void newTable(int boxes) {
		int id = OpenBJS.INSTANCE.getTableManager().launchTable(new Table(boxes));
		logger.info("New table successfully created with id " + id);
	}
	
	private void removeTable(int id) {
		if (OpenBJS.INSTANCE.getTableManager().removeTable(id)) {
			logger.info("Table was removed successfully");
		} else {
			logger.info("There is no table with id '" + id + "'");
		}
	}
	
	private static enum SubCommand {
		NEW		(args -> {
			try {
				Integer.parseInt(args.get(0));
				return true;
			} catch (Throwable e) {
				return false;
			}
		}),
		STOP	(NEW.checkArgs);
		
		public final Function<List<String>, Boolean> checkArgs;
		
		SubCommand(Function<List<String>, Boolean> argValidator) {
			checkArgs = argValidator;
		}
	}
}
