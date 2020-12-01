package thedarkdnktv.openbjs.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.filter.MarkerFilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import thedarkdnktv.openbjs.api.API;

/** Main configuration file of server
 * 
 * @author TheDarkDnKTv
 *
 */
public class Config {
	private static transient Path configFile;
	private static transient Gson GSON;
	
	// A API-key for true random
	private boolean useRadnomOrg;
	private String RANDOM_ORG_API_KEY;

	// Debugging
	public final boolean DEBUG;
	public final boolean NET_DEBUG ;
	
	// Main server arguments
	private int DECK_SIZE;
	private int DECKS;
	private int TICK_PERIOD;
	
	private transient int CARDS = DECKS * DECK_SIZE;
	
	// For private use only
	private Config() {
		DEBUG = false;
		NET_DEBUG = false;
		
		DECK_SIZE = 52;
		DECKS = 8;
		TICK_PERIOD = 100;
		
		RANDOM_ORG_API_KEY = "";
		useRadnomOrg = false;
	}
	
	public static Config init(Logger logger) {
		Config result = null;
		
		if (Files.exists(configFile)) {
			try (FileReader reader = new FileReader(configFile.toFile())) {
				result = GSON.fromJson(reader, Config.class);
			} catch (Throwable e) {
				logger.warn("Unable to load existing config");
				logger.catching(Level.DEBUG, e);
				result = new Config();
			}
		} else {
			result = new Config();
			try (FileWriter writer = new FileWriter(configFile.toFile())) {
				GSON.toJson(result, writer);
			} catch (Throwable e) {
				logger.fatal("Unable to write configuration file");
				logger.catching(Level.DEBUG, e);
			}
		}
		
		if (!result.DEBUG) {
			MarkerFilter filter = MarkerFilter.createFilter("NETWORK", Result.DENY, Result.NEUTRAL);
			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
	        ctx.getConfiguration().addFilter(filter);
		}
		
		try {
			Class<?> clz = Class.forName("thedarkdnktv.openbjs.api.API", true, Config.class.getClassLoader());
			Field fDebug 	= clz.getField("DEBUG");
			Field fNetDebug = clz.getField("NETWORK_DEBUG");
			Field fMods 	= Field.class.getDeclaredField("modifiers");
			fDebug		.setAccessible(true);
			fNetDebug	.setAccessible(true);
			fMods		.setAccessible(true);
			fMods.setInt(fDebug,	 fDebug.getModifiers() & ~Modifier.FINAL);
			fMods.setInt(fNetDebug,	 fNetDebug.getModifiers() & ~Modifier.FINAL);
			fDebug		.setBoolean(null, result.DEBUG);
			fNetDebug	.setBoolean(null, result.NET_DEBUG);
		} catch (Throwable ignored) {
			logger.catching(Level.DEBUG, ignored);
			ignored.printStackTrace();
		}
		
		if (API.DEBUG) {
			logger.info("Debug mode activated");
			if (API.NETWORK_DEBUG) logger.warn("Network debug activated, please be aware of console spam"); 
		}
		
		return result;
	}
	
	
	
	/* GETTERS */
	
	/**
	 * @return a base game settings
	 */
	public GameplaySettings getGameSettings() {
		return new GameplaySettings(DECK_SIZE, DECKS, CARDS);
	}
	
	/**
	 * @return total amount of playing cards in shoe
	 */
	public int getTickTime() {
		return TICK_PERIOD;
	}
	
	/**
	 * @return null or API key
	 */
	public String getRandomAPIKey() {
		return RANDOM_ORG_API_KEY;
	}
	
	public boolean getRandomAvailable() {
		return useRadnomOrg && RANDOM_ORG_API_KEY != null && !RANDOM_ORG_API_KEY.isEmpty();
	}
	
	static {
		configFile = new File("server.json").toPath();
		GSON = new GsonBuilder()
				.setPrettyPrinting()
				.serializeNulls()
				.disableHtmlEscaping()
				.create();
	}
	
	/**
	 * A wrapper for all basic game settings
	 * @author TheDarkDnKTv
	 *
	 */
	public static class GameplaySettings {
		public final int DECK_SIZE;
		public final int DECKS;
		public final int CARDS;
		
		private GameplaySettings(int deckSize, int decks, int cards) {
			DECK_SIZE = deckSize;
			DECKS = decks;
			CARDS = cards;
		}
	}
}
