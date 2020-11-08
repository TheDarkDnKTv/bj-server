package thedarkdnktv.openbjs.manage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import thedarkdnktv.openbjs.OpenBJS;
import thedarkdnktv.openbjs.command.CommandStop;
import thedarkdnktv.openbjs.command.ICommand;
import thedarkdnktv.openbjs.exception.CommandExecuteException;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class CommandManager implements Runnable {
	private static final List<ICommand> commandRegistry = new ArrayList<>();
	
	private final List<String> pendingCommands = Collections.synchronizedList(new ArrayList<>());
	
	public CommandManager() {
		Thread thread = new Thread(this, "Console handler");
		thread.setDaemon(true);
		thread.start();
	}
	
	/**
	 * Registering commands
	 */
	public static void init() {
		registerCommand(new CommandStop());
	}
	
	public void executePendingCommands() {
		if (!pendingCommands.isEmpty()) {
			String command = pendingCommands.remove(0);
			
			try {
				this.processCommand(new ArrayList<>(Arrays.asList(command.split(" "))));
			} catch (CommandExecuteException e) {
				OpenBJS.info("Exception occurew trying execute command");
				e.printStackTrace();
			}
		}
	}
	
	public void processCommand(List<String> lines) throws CommandExecuteException {
		String comName = lines.remove(0);
		ICommand command = this.findCommand(comName);
		if (command != null) {
			try {
				if (command.isValidInput(lines)) {
					command.execute(lines);
				} else {
					OpenBJS.info(command.getUsageString());
				}
			} catch (Throwable e) {
				throw new CommandExecuteException("command: " + comName, e);
			}
		} else {
			OpenBJS.info("Unknown command: " + comName);
		}
	}
	
	public ICommand findCommand(String name) {
		Objects.requireNonNull(name, "Command name cannot be null");
		ICommand command = null;
		
		if (!name.isEmpty()) {
			for (ICommand com : commandRegistry) {
				for (String n : com.getNames()) {
					if (name.equals(n)) {
						return com;
					}
				}
			}
		} else {
			throw new IllegalArgumentException("Command name cannot be empty");
		}
		
		return command;
	}
	
	private static void registerCommand(ICommand command) {
		commandRegistry.add(command);
	}
	
	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));) {
			while (OpenBJS.INSTANCE.isRunning()) {
				if (reader.ready()) {
					String command = reader.readLine();
					if (command != null && !command.isEmpty()) pendingCommands.add(command);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			// TODO
		}
	}
}
