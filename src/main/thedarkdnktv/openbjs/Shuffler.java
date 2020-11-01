package thedarkdnktv.openbjs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class Shuffler {
	
	private static Random random;
	
	public static void initRandom() {
		if (random == null) {
//			try {
//				HttpURLConnection con = (HttpURLConnection) URI.create("https://api.random.org/json-rpc/1/invoke").toURL().openConnection();
//				con.setDoOutput(true);
//				con.setRequestMethod("POST");
//				con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//				con.connect();
//				
//				JsonObject obj = new JsonObject();
//				JsonObject params = new JsonObject();
//				
//				params.add("apiKey", new JsonPrimitive("7b57e9cc-6ebe-4c05-9456-a47e4e02ac62"));
//				params.add("n", new JsonPrimitive(1));
//				params.add("min", new JsonPrimitive(100000));
//				params.add("max", new JsonPrimitive(1000000000));
//				
//				obj.add("jsonrpc", new JsonPrimitive("2.0"));
//				obj.add("method", new JsonPrimitive("generateIntegers"));
//				obj.add("id", new JsonPrimitive(0));
//				obj.add("params", params);
//				
//				try (OutputStream out = con.getOutputStream()) {
//					out.write(obj.toString().getBytes(StandardCharsets.UTF_8));
//				}
//				
//				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//				String answer = in.readLine();
//				con.disconnect();
//				
//                try {
//            		JsonObject result = new JsonParser().parse(answer).getAsJsonObject();
//            		long seed = result.get("result").getAsJsonObject().get("random").getAsJsonObject().get("data").getAsJsonArray().get(0).getAsLong();
//            		OpenBJS.info("Random seed: " + seed);
//            		
//            		random = new Random(seed);
//            	} catch (Throwable e) {
//            		throw new IOException();
//            	}
//			} catch (IOException e) {
//				e.printStackTrace();
//				System.exit(-1);
//			}
			random = new Random();
		} else {
			random = new Random(random.nextLong());
		}
		
		OpenBJS.info("Random successfully initialized");
	}
	
	public static Queue<Card> getNewShoe() {
		Queue<Card> shoe = new ArrayBlockingQueue<>(OpenBJS.CARDS + 1, true);
		
		for (int i = 1; i <= 8; i++) {
			Collection<Card> deck = Card.getDeck(i);
			shoe.addAll(deck);
		}
		
		return shoe;
	}
	
	public static Queue<Card> chemmyShuffle(Collection<Card> shoe) {
		List<Card> sorted = shoe.stream()
			.map(card -> new Pair<>(card, random.nextLong()))
			.sorted(Comparator.comparingLong(pair -> pair.getValue()))
			.map(pair -> pair.getKey())
			.collect(Collectors.toList());
		sorted.add(OpenBJS.CARDS / 2 + getHalfOffset(), Card.CUTTING_CARD);
		return new ArrayBlockingQueue<>(OpenBJS.CARDS + 1, true, sorted);
	}
	
	public static Queue<Card> shuffle(Collection<Card> mainShoe) {
		List<Card> shoe = new ArrayList<>(mainShoe),
				towerMain = new ArrayList<>(),
				towerLeft = new ArrayList<>(),
				towerRight = new ArrayList<>();
		
		shoe.removeIf(card -> card == Card.CUTTING_CARD);
		
		int offset = getHalfOffset();
		towerLeft.addAll(shoe.subList(shoe.size() / 2 + offset, shoe.size()));
		towerRight.addAll(shoe.subList(0, shoe.size() / 2 + offset));
		towerMain.clear();
		
		OpenBJS.info("SIZE: " + (towerLeft.size() + towerRight.size()));
		
		return null;
	}
	
	private static int getHalfOffset() {
		return random.nextInt(34) - 17;
	}
	
	static {
		initRandom();
	}
	
	private static class Pair<K, V> implements Map.Entry<K, V> {
		K key;
		V value;
		
		public Pair(K key, V value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}
	}
}
