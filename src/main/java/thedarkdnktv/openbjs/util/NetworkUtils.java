package thedarkdnktv.openbjs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * @author TheDarkDnKTv
 *
 */
public class NetworkUtils {
	private static final Logger logger = LogManager.getLogger();
	
	public static URL from(String url) {
		try {
			URL result = new URL(url);
			return result;
		} catch (MalformedURLException e) {
			logger.catching(e);
		}
		
		return null;
	}
	
	public static JsonObject parseToJson(byte[] data) {
		try {
			String jsonData = new String(data, StandardCharsets.UTF_8);
			JsonObject result = new JsonParser().parse(jsonData).getAsJsonObject();
			return result;
		} catch (Throwable e) {
			logger.catching(e);
		}
		
		return null;
	}
	
	/**
	 * Simple POST request with raw data with 512KB buffer
	 * @param host the URL to post
	 * @param contentType - like application/json etc, without any characters at end. Could be used array of values (application/json,text/html)
	 * @param rawData the data
	 * @return data
	 */
	public static byte[] post(URL host, String contentType, byte[] rawData) {
		return post(host, contentType, rawData, 0x80000);
	}
	
	/**
	 * Simple POST request with raw data
	 * @param host the URL to post
	 * @param contentType - like application/json etc, without any characters at end. Could be used array of values (application/json,text/html)
	 * @param rawData the data
	 * @param answerMaxLenght the lenght of answer in bytes, to prevent cutting of from buffered stream
	 * @return data
	 */
	public static byte[] post(URL host, String contentType, byte[] rawData, int answerMaxLenght) {
		try {
			HttpURLConnection con = (HttpURLConnection) host.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", contentType + "; charset=UTF-8");
			con.connect();
			
			try (OutputStream out = con.getOutputStream()) {
				out.write(rawData);
			}
			
			byte[] result = new byte[0];
			try (InputStream in = con.getInputStream()) {
				result = new byte[in.available()];
				while (in.read(result) != -1);
			}

			
			con.disconnect();
			return result;
		} catch (IOException e) {
			logger.catching(e);
		}
		
		return new byte[0];
	}
	
	// TODO
	public static InputStream post(URL host, Object...request) {
		return nullInputStream();
	}
	
	private static InputStream nullInputStream() {
		return new InputStream() {
			@Override
			public int read() throws IOException {
				return -1;
			}
		};
	}
}
