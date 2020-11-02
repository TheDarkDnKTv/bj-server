package thedarkdnktv.openbjs.network;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NetHandler {
	
	public JsonObject parseToJson(byte[] data) {
		try {
			String jsonData = new String(data, StandardCharsets.UTF_8);
			JsonObject result = new JsonParser().parse(jsonData).getAsJsonObject();
			return result;
		} catch (Throwable e) {
			e.printStackTrace();
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
	public InputStream post(URL host, String contentType, byte[] rawData) {
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
	public InputStream post(URL host, String contentType, byte[] rawData, int answerMaxLenght) {
		try {
			HttpURLConnection con = (HttpURLConnection) host.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", contentType + "; charset=UTF-8");
			con.connect();
			
			try (OutputStream out = con.getOutputStream()) {
				out.write(rawData);
			}
			
			InputStream result = new BufferedInputStream(con.getInputStream(), answerMaxLenght); // TODO could be triky and not saves data
			con.disconnect();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return nullInputStream();
	}
	
	// TODO
	public InputStream post(URL host, Object...request) {
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
