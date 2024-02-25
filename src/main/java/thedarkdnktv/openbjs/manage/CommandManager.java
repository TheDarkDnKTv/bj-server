package thedarkdnktv.openbjs.manage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thedarkdnktv.openbjs.OpenBJS;
import thedarkdnktv.openbjs.command.CommandStatus;
import thedarkdnktv.openbjs.command.CommandStop;
import thedarkdnktv.openbjs.command.CommandTable;
import thedarkdnktv.openbjs.command.ICommand;
import thedarkdnktv.openbjs.exception.CommandExecuteException;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class CommandManager implements Runnable {
	private static final Map<String ,ICommand> commandRegistry = new HashMap<>();
	private static final Logger logger = LogManager.getLogger();
	
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
		registerCommand(new CommandTable());
		registerCommand(new CommandStatus());
	}
	
	public void executePendingCommands() {
		if (!pendingCommands.isEmpty()) {
			String command = pendingCommands.remove(0);
			
			try {
				this.processCommand(new ArrayList<>(Arrays.asList(command.split(" "))));
			} catch (CommandExecuteException e) {
				logger.fatal("Exception occured trying execute command - '" + e.getMessage() + "'");
				logger.catching(e);
			}
		}
	}
	
	public void processCommand(List<String> lines) throws CommandExecuteException {
		String comName = lines.remove(0);
		ICommand command = this.findCommand(comName);
		if (command != null) {
			try {
				if (command.isValidInput(new ArrayList<>(lines))) {
					command.execute(lines);
				} else {
					logger.info(command.getUsageString());
				}
			} catch (Throwable e) {
				throw new CommandExecuteException("command: " + comName, e);
			}
		} else {
			if (comName.equals("?") || comName.equalsIgnoreCase("help")) {
				StringBuffer buf = new StringBuffer();
				buf.append("Next commands available: ");
				commandRegistry.keySet().forEach(c -> buf.append(c + " "));
				logger.info(buf.toString());
			} else {
				logger.error("Unknown command: " + comName);
			}
		}
	}
	
	public ICommand findCommand(String name) {
		Objects.requireNonNull(name, "Command name cannot be null");
		
		if (!name.isEmpty()) {
			return commandRegistry.get(name);
		} else {
			throw new IllegalArgumentException("Command name cannot be empty");
		}
	}
	
	private static void registerCommand(ICommand command) {
		List<String> names = Arrays.asList(command.getNames());
		if (!commandRegistry.keySet().containsAll(names)) {
			for (String name : names) {
				commandRegistry.put(name, command);
			}
		} else {
			logger.error("Unable to register command " + names + ", some of name already registered");
		}
	}
	
	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));) {
			while (OpenBJS.INSTANCE == null) {
				Thread.sleep(50);
			}
			
			while (OpenBJS.INSTANCE.isRunning()) {
				if (reader.ready()) {
					String command = reader.readLine();
					if (command != null && !command.isEmpty()) pendingCommands.add(command);
				}
			}
		} catch (Throwable e) {
			logger.catching(e);
		}
	}
}
