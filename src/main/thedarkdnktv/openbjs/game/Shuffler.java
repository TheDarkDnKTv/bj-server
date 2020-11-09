package thedarkdnktv.openbjs.game;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import thedarkdnktv.openbjs.OpenBJS;
import thedarkdnktv.openbjs.exception.ShoeNotValidException;
import thedarkdnktv.openbjs.exception.WrongShuffleException;
import thedarkdnktv.openbjs.network.NetHandler;

/**
 * Provides all functionallity related with shuffler work:<br>
 * <ul>
 * 	<li>new shoe creation</li>
 * 	<li>chemmy shuffle procedure</li>
 *  <li>shuffle procedure</li>
 *  <li>shoe validation</li>
 * </ul>
 * @author TheDarkDnKTv
 *
 */
public class Shuffler {
	
	private static final Logger logger = LogManager.getLogger();
	
	private static Random random;
	protected static final Set<Card> STANDARD;
	
	static {
		Set<Card> shoe = new HashSet<>(getNewShoe());
		STANDARD = Collections.unmodifiableSet(shoe);
	}
	
	public static void initRandom() {
		if (random == null) {
			if (!OpenBJS.DEBUG) {
				JsonObject obj = new JsonObject();
				JsonObject params = new JsonObject();
				
				params.add("apiKey", new JsonPrimitive(OpenBJS.RANDOMORG_API_KEY));
				params.add("n", new JsonPrimitive(1));
				params.add("min", new JsonPrimitive(100000));
				params.add("max", new JsonPrimitive(1000000000));
				
				obj.add("jsonrpc", new JsonPrimitive("2.0"));
				obj.add("method", new JsonPrimitive("generateIntegers"));
				obj.add("id", new JsonPrimitive(0));
				obj.add("params", params);
				
				byte[] data = OpenBJS.net.post(NetHandler.from("https://api.random.org/json-rpc/1/invoke"), "application/json", obj.toString().getBytes(StandardCharsets.UTF_8));
				
				try {
					JsonObject answer = OpenBJS.net.parseToJson(data);
					long seed = answer.get("result").getAsJsonObject().get("random").getAsJsonObject().get("data").getAsJsonArray().get(0).getAsLong();
	        		logger.info("Random seed: " + seed);
	        		random = new Random(seed);
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			} else random = new Random();
		} else {
			random = new Random(random.nextLong());
		}
		
		logger.info("Random successfully initialized");
	}
	
	/**
	 * Get a new shoe after card change
	 * @return
	 */
	public static Shoe getNewShoe() {
		Shoe shoe = new Shoe();
		
		for (int i = 1; i <= 8; i++) {
			Collection<Card> deck = Card.getDeck(i);
			shoe.addAll(deck);
		}
		
		return shoe;
	}
	
	/**
	 * Randomize the card positions in shoe
	 */
	public static Shoe chemmyShuffle(Collection<? extends Card> shoe) {
		List<Card> sorted = shoe.stream()
			.map(card -> new Pair<>(card, random.nextLong()))
			.sorted(Comparator.comparingLong(pair -> pair.getValue()))
			.map(pair -> pair.getKey())
			.collect(Collectors.toList());
		return new Shoe(sorted);
	}
	
	/**
	 * Real shuffle procedure, shuffling the shoe.
	 */
	public static Shoe shuffle(Collection<? extends Card> mainShoe) {
		List<Card> shoe = new ArrayList<>(mainShoe);
		shoe.removeIf(card -> card == Card.CUTTING_CARD);
		
		if (!validateShoe(shoe))
			throw new ShoeNotValidException("Not valid shoe", new IllegalArgumentException());
		
		shuffleRound(shoe, Shuffler::riffle);
		shuffleRound(shoe, Shuffler::riffle);
		shuffleRound(shoe, Shuffler::riffleStripRiffle);
			
		return prepareForGame(shoe);
	}
	
	/**
	 * Validation of shoe, check for no duplicates and all cards presented
	 * @param shoe without CUTTING Card
	 */
	public static <C extends Card> boolean validateShoe(Collection<C> shoe) {
		if (shoe.contains(Card.CUTTING_CARD))
			throw new IllegalArgumentException("Shoe contains CUTTING card");
		Set<C> shoe1 = new HashSet<>(shoe);
		return shoe1.containsAll(STANDARD) && shoe.size() == shoe1.size();
	}
	
	private static <C extends Card> Shoe prepareForGame(List<C> shoe) {
		List<Card> temp = new ArrayList<>();
		int position = shoe.size() / 2 + getHalfOffset();
		temp.addAll(shoe.subList(position, shoe.size()));
		shoe.removeAll(temp);
		temp.addAll(shoe);
		shoe.clear();
		position = temp.size() / 2 + getHalfOffset();
		temp.add(position, Card.CUTTING_CARD);
		Shoe shoe1 = new Shoe(temp);
		if (shoe1.validate()) 
			throw new ShoeNotValidException(new WrongShuffleException("Shoe size: " + shoe1.size() + ", values: " + shoe1.toString()));
		shoe1.setShuffled();
		return shoe1;
	}
	
	/**
	 * Generic method make shuffle round
	 * @param shoe main tower to shuffle
	 * @param action - action perfomed on every half-decks of this tower
	 */
	private static <C extends Card> void shuffleRound(List<C> shoe, BiFunction<List<C>, List<C>, List<C>> action) {
		List<C> left = new ArrayList<>(), right = new ArrayList<>();
		splitTower(shoe, left, right);
		shoe.clear();
		
		while (left.size() > 0 || right.size() > 0) {
			List<C> a = null, b = null;
			double deckMult = OpenBJS.DECK_SIZE * 0.75D;
			if (left.size() > deckMult && right.size() > deckMult) {
				a = splitDeck(left);
				b = splitDeck(right);
			} else {
				left.addAll(right);
				a = splitDeck(left);
				b = new ArrayList<>(left);
				left.clear();
				right.clear();
			}
			
			shoe.addAll(action.apply(a, b));
		}
	}
	
	/**
	 * Splitting the main shoe tower to two smaller towers
	 */
	private static <C extends Card> void splitTower(List<C> main, List<C> left, List<C> right) {
		int position = main.size() / 2 + getHalfOffset();
		left.clear();
		right.clear();
		left.addAll(main.subList(0, position));
		right.addAll(main.subList(position, main.size()));
	}
	
	/**
	 * Taking cards half of deck size from tower
	 */
	private static <C extends Card> List<C> splitDeck(List<C> halfTower) {
		List<C> result = new ArrayList<>();
		int deckMult = (int) (OpenBJS.DECK_SIZE * 0.175D);
		int randOffset = random.nextInt(deckMult) - (deckMult / 2);
		int position = halfTower.size() - (OpenBJS.DECK_SIZE / 2 + randOffset);
		result = new ArrayList<>(halfTower.subList(position < 0 ? 0 : position, halfTower.size()));
		halfTower.removeAll(result);
		return result;
	}
	
	/**
	 * The third round of shuffle, perfoming a riffle, strip and riffle
	 */
	private static <C extends Card> List<C> riffleStripRiffle(List<C> left, List<C> right) {
		List<C> temp = riffle(left, right);
		strip(temp);
		int middle = temp.size() / 2;
		int randomOffset = random.nextInt(OpenBJS.DECK_SIZE / 12) - OpenBJS.DECK_SIZE / 6;
		left.addAll(temp.subList(0, middle + randomOffset));
		right.addAll(temp.subList(middle + randomOffset, temp.size()));
		temp = riffle(left, right);
		
		return temp;
	}
	
	/**
	 * Riffle the two half-decks
	 * @return the resulting riffled full deck
	 */
	private static <C extends Card> List<C> riffle(List<C> left, List<C> right) {
		List<C> result = new ArrayList<>();
		Iterator<C> iterL = left.iterator(), iterR = right.iterator();
		int randOffset = 0;
		while (iterL.hasNext() || iterR.hasNext()) {
			for (randOffset = random.nextInt(3) + 1; randOffset > 0 && iterL.hasNext(); randOffset--) {
				result.add(iterL.next());
				iterL.remove();
			}
			
			for (randOffset = random.nextInt(3) + 1; randOffset > 0 && iterR.hasNext(); randOffset--) {
				result.add(iterR.next());
				iterR.remove();
			}
		}
		
		return result;
	}
	
	/**
	 * Made a strip for deck
	 */
	private static <C extends Card> void strip(List<C> deck) {
		List<C> temp = new ArrayList<>();
		if (deck.size() < OpenBJS.DECK_SIZE / 4) {
			throw new IllegalArgumentException("Can not make strip from such small deck! Deck size: " + deck.size());
		}
		
		int stripAmount = random.nextInt(3) + 5;
		int avgAmount = OpenBJS.ceil(deck.size() * 1.0D / stripAmount);
		while (stripAmount > 0 || !deck.isEmpty()) {
			int offset = random.nextInt(avgAmount / 2) - avgAmount / 4;
			int position = deck.size() - (avgAmount + offset);
			List<C> temp1 = new ArrayList<>(stripAmount > 1 ? deck.subList(position < 0 ? 0 : position, deck.size()) : deck);
			deck.removeAll(temp1);
				temp.addAll(temp1);
			stripAmount--;
		}
		
		deck.clear();
		deck.addAll(temp);
	}
	
	/**
	 * Half of tower offset
	 * @return
	 */
	private static int getHalfOffset() {
		int offset = (int) (OpenBJS.CARDS * 0.05D);
		return random.nextInt(offset * 2) - offset;
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
