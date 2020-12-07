package thedarkdnktv.openbjs.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.filter.MarkerFilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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
	private Property<Boolean> useRandomOrg;
	private Property<String> randomOrgApiKey;
	private Property<InetAddress> address;
	private Property<Integer> port;
	private Property<Boolean> useEpoll;

	// Debugging
	public final Property<Boolean> DEBUG;
	public final Property<Boolean> NET_DEBUG;
	
	// Main server arguments
	private Property<Integer> DECK_SIZE;
	private Property<Integer> DECKS;
	private Property<Integer> TICK_PERIOD;
	
	
	// For private use only
	private Config() {
		this(false, false);
	}
	
	private Config(boolean debug, boolean netDebug) {
		DECK_SIZE 			= Property.of("server.deck_size", 52);
		DECKS 				= Property.of("server.decks_amount", 8);
		TICK_PERIOD 		= Property.of("server.tick_time", 100);
		
		DEBUG 				= Property.of("server.debug", debug);
		NET_DEBUG			= Property.of("server.debug.network", netDebug);
		
		useRandomOrg 		= Property.of("random_org.use", false);
		randomOrgApiKey 	= Property.of("random_org.api_key", "");
		address				= Property.of("server.ip", InetAddress.getLoopbackAddress());
		port 				= Property.of("server.port", 0x7FF8); // 32760 - default port
		useEpoll 			= Property.of("server.use.native_transport", true);
	}
	
	public static Config init(Logger logger) {
		Config result = null;
		
		if (Files.exists(configFile)) {
			try (FileReader reader = new FileReader(configFile.toFile())) {
				result = GSON.fromJson(reader, Config.class);
			} catch (Throwable e) {
				logger.warn("Unable to load existing config");
				try {
					Files.delete(configFile);
				} catch (IOException e1) {}
				result = new Config().createConfig(logger);
			}
		} else {
			result = new Config().createConfig(logger);
		}
		
		if (!result.DEBUG.value) {
			MarkerFilter filter = MarkerFilter.createFilter("NETWORK", Result.DENY, Result.NEUTRAL);
			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
	        ctx.getConfiguration().addFilter(filter);
		}
		
		try {
			Class<?> clz = Class.forName("thedarkdnktv.openbjs.api.API", true, Config.class.getClassLoader());
			Field fDebug 	= clz.getField("DEBUG");
			Field fNetDebug = clz.getField("NETWORK_DEBUG");
			API.setFinal(fDebug, result, Boolean.valueOf(result.DEBUG.value));
			API.setFinal(fNetDebug, result, Boolean.valueOf(result.NET_DEBUG.value));
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
	
	private Config createConfig(Logger logger) {
		try (FileWriter writer = new FileWriter(configFile.toFile())) {
			GSON.toJson(this, writer);
		} catch (Throwable e) {
			logger.fatal("Unable to write configuration file");
			logger.catching(Level.DEBUG, e);
		}
		
		return this;
	}
	
	/* GETTERS */
	
	/**
	 * @return a base game settings
	 */
	public GameplaySettings getGameSettings() {
		return new GameplaySettings(DECK_SIZE.value, DECKS.value, this.getCardsTotal());
	}
	
	/**
	 * @return total amount of playing cards in shoe
	 */
	public int getTickTime() {
		return TICK_PERIOD.value;
	}
	
	public int getCardsTotal() {
		return DECKS.value * DECK_SIZE.value;
	}
	
	public InetAddress getServerAddress() {
		return address.value;
	}
	
	public int getServerPort() {
		return port.value;
	}
	
	/**
	 * @return null or API key
	 */
	public String getRandomAPIKey() {
		return randomOrgApiKey.value;
	}
	
	public boolean getRandomAvailable() {
		return useRandomOrg.value && !getRandomAPIKey().isEmpty();
	}
	
	public boolean isUsingEpoll() {
		return useEpoll.value;
	}
	
	static {
		configFile = new File("server.json").toPath();
		GSON = new GsonBuilder()
				.registerTypeAdapter(Config.class, new Jsoner())
				.setPrettyPrinting()
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
	
	/**
	 * A wrapper for value, containing name for config
	 * @author TheDarkDnKTv
	 */
	private static abstract class Property<T> {
		final T value;
		
		public static <T> Property<T> of(String name, T value) {
			return new Property<T>(value) {
				@Override
				public String getName() {
					return name;
				}
			};
		}
		
		Property(T value) {
			this.value = value;
		}
		
		public abstract String getName();
		
		@Override
		public String toString() {
			return getName() + "=" + value;
		}
	}
	
	private static class Jsoner implements JsonSerializer<Config>, JsonDeserializer<Config> {
		@Override
		public Config deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (!json.isJsonObject()) {
				throw new JsonParseException("Not a object");
			} else {
				Config result = new Config();
				JsonObject jobj = json.getAsJsonObject();
				Map<String, Field> fields = Arrays.asList(Config.class.getDeclaredFields()).stream()
						.filter(field -> field.getType() == Property.class)
						.collect(HashMap::new,
								(map, field) -> map.put(API.<Property<?>>getFieldValue(field, result).getName(), field),
								HashMap::putAll);
				for (Entry<String, Field> entry : fields.entrySet()) {
					Class<?> clz = API.<Property<?>>getFieldValue(entry.getValue(), result).value.getClass();
					Object value = context.deserialize(jobj.get(entry.getKey()), clz);
					if (value != null) {
						API.setFinal(entry.getValue(), result, Property.<Object>of(entry.getKey(), value));
					} else {
						throw new JsonParseException("Field not found " + entry.getKey());
					}
				}
				
				return result;
			}
		}

		@Override
		public JsonElement serialize(Config src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			Arrays.asList(Config.class.getDeclaredFields()).stream()
				.filter(field -> field.getType() == Property.class)
				.map(field -> API.<Property<?>>getFieldValue(field, src))
				.sorted(Comparator.comparing(prop -> prop.getName()))
				.forEach(prop -> obj.add(prop.getName(), Config.GSON.toJsonTree(prop.value)));
			return obj;
		}
	}
}
