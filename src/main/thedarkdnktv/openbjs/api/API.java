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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thedarkdnktv.openbjs.api.annotation.Client;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class API {
	public static final String Version = "1.0.0";
	
	private static final Map<String, IClient> REGISTRY = new HashMap<>();
	private static final Logger logger = LogManager.getLogger();
	
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
			if (IClient.class.isAssignableFrom(cl)) {
				try {
					IClient instance = IClient.class.cast(cl.newInstance());
					String id = cl.getAnnotation(Client.class).clientId();
					API.addClient(id, instance);
				} catch (Throwable e) {
					logger.error("Failed to initialize client " + cl.toString());
					logger.catching(e);
				}
			}
		}
		
		if (!REGISTRY.isEmpty()) {
			logger.info("Registered " + REGISTRY.size() + " API-based clients");
		} else {
			logger.info("No API-based clients was found");
		}
	}
	
	
	public static boolean addClient(String clientId, IClient client) {
		Objects.requireNonNull(clientId);
		Objects.requireNonNull(client);
		if (!clientId.isEmpty() && !REGISTRY.containsKey(clientId)) {
			REGISTRY.put(clientId, client);
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
	
	
}
