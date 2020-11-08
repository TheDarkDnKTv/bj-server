package thedarkdnktv.openbjs.command;

import java.util.List;

/**
 * Interface used to make a command used in server's console
 * 
 * @author TheDarkDnKTv
 *
 */
public interface ICommand {
	
	/**
	 * Any of strings from array could be used to run this command
	 * @return array of Strings, min.lenght = 1
	 */
	public String[] getNames();
	
	/**
	 * Returns true if given command line properties valid, otherwise will be called {@link ICommand#getUsageString()}
	 */
	public boolean isValidInput(List<String> properties);
	
	/**
	 * Called when given properties not valid. Shows in console as tip.
	 */
	public String getUsageString();
	
	/**
	 * 
	 * @param properties from console input
	 */
	public void execute(List<String> properties);
}
