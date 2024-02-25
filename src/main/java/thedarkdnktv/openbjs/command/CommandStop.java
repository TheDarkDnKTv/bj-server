package thedarkdnktv.openbjs.command;

import java.util.List;

import thedarkdnktv.openbjs.OpenBJS;

/**
 * @author TheDarkDnKTv
 *
 */
public class CommandStop implements ICommand {

	@Override
	public String[] getNames() {
		return new String[]{"stop", "shutdown", "exit"};
	}

	@Override
	public boolean isValidInput(List<String> properties) {
		return true;
	}

	@Override
	public String getUsageString() {
		return null;
	}

	@Override
	public void execute(List<String> properties) {
		if (OpenBJS.INSTANCE != null) {
			OpenBJS.INSTANCE.stop();
		} else {
			// TODO "Server is not running error"
		}
	}
}
