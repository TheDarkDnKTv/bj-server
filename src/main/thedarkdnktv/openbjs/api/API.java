package thedarkdnktv.openbjs.api;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thedarkdnktv.openbjs.api.annotation.Client;
import thedarkdnktv.openbjs.api.interfaces.IInitializable;
import thedarkdnktv.openbjs.api.interfaces.IServer;
import thedarkdnktv.openbjs.api.interfaces.ITickable;
import thedarkdnktv.openbjs.api.util.ThreadFactoryBuilder;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class API {
	public static final String VERSION = "1.2.1";
	
	private static final Logger logger;
	private static final Map<String, Object> REGISTRY;
	private static final Map<String, ITickable> TICKABLES;
	
	public static final boolean DEBUG;
	public static final boolean NETWORK_DEBUG;
	
	static {
		logger = LogManager.getLogger();
		REGISTRY = new HashMap<>();
		TICKABLES = new HashMap<>();
		DEBUG = false;
		NETWORK_DEBUG = false;
	}
	
	public static void init() {
		URLClassLoader cloader = (URLClassLoader) API.class.getClassLoader();
		List<Class<?>> clients = new ArrayList<>();
		for (URL url : cloader.getURLs()) {
			try {
				Path path = Paths.get(url.toURI());
				List<String> classes = new ArrayList<>();
				if (Files.isDirectory(path)) {
					classes = Files.walk(path)
							.filter(p -> p.getFileName().toString().endsWith("class"))
							.map(p -> path.relativize(p).toString())
							.map(s -> s.replace(".class", "").replace("\\", ".").replace("/", "."))
							.collect(Collectors.toList());
					
				} else {
					String fileName = path.getFileName().toString();
					if (fileName.endsWith("jar") || fileName.endsWith("zip")) {
						ZipFile archive = new ZipFile(path.toFile());
						classes = archive.stream()
								.filter(e -> e.getName().endsWith("class"))
								.map(e -> e.getName().replace(".class", ""))
								.map(s -> s.replace(".class", "").replace("\\", ".").replace("/", "."))
								.collect(Collectors.toList());
					}
				}
				
				for (String clz : classes) {
					Class<?> clazz = Class.forName(clz, false, cloader);
					if (clazz.getAnnotation(Client.class) != null) {
						clients.add(clazz);
					}
				}
			} catch (Throwable e) {}
		}
		
		for (Class<?> cl : clients) {
			try {
				Object instance = cl.newInstance();
				String id = cl.getAnnotation(Client.class).clientId();
				API.addClient(id, instance);
			} catch (Throwable e) {
				logger.error("Failed to initialize client " + cl.toString());
				logger.catching(e);
			}
		}
		
		if (!REGISTRY.isEmpty()) {
			logger.info("Registered " + REGISTRY.size() + " API-based clients");
		} else {
			logger.info("No API-based clients was found");
		}
	}
	
	public static boolean addClient(String clientId, Object client) {
		Objects.requireNonNull(clientId);
		Objects.requireNonNull(client);
		if (!clientId.isEmpty() && !REGISTRY.containsKey(clientId)) {
			REGISTRY.put(clientId, client);
			if (client instanceof ITickable)
				TICKABLES.put(clientId, (ITickable)client);
			return true;
		}
		
		return false;
	}
	
	public static boolean removeClient(String clientId) {
		if (clientId != null && !clientId.isEmpty()) {
			return REGISTRY.remove(clientId) != null;
			
		}
		
		return false;
	}
	
	public static void initClients() {
		for (Entry<String, ITickable> entry : TICKABLES.entrySet()) {
			if (entry.getValue() instanceof IInitializable) {
				try {
					((IInitializable) entry.getValue()).init();
				} catch (Throwable e) {
					logger.error("Init error of %s %s", entry.getKey(), e);
				}
			}
		}
	}
	
	public static void runClients(IServer server) {
		ThreadFactory factory = new ThreadFactoryBuilder()
				.setNameFormat("Client#%d")
				.setUncaughtExceptionHandler(server.getExceptionHandler())
				.setDeamon(true)
				.build();
		
		for (ITickable cl : TICKABLES.values()) {
			factory.newThread(() -> {
				while(server.isRunning()) {
					cl.update();
				}
			}).start();
		}
	}
}
