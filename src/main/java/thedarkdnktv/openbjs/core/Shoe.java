package thedarkdnktv.openbjs.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thedarkdnktv.openbjs.game.Card;

import java.util.*;
import java.util.stream.Collectors;

import static thedarkdnktv.openbjs.Logging.SHOE_EVENTS;

public final class Shoe implements IShoe<AbstractCard>, Identifiable {

    private final static Logger LOG = LogManager.getLogger();

    private final LinkedList<AbstractCard> shoe = new LinkedList<>();
    private final Set<ICard> deckSample;
    private final int deckCount;
    private final int id;

    private boolean needShuffle;
    private boolean valid;

    public Shoe(int id, int deckCount, Set<ICard> deck) {
        this.deckSample = Collections.unmodifiableSet(deck);
        this.deckCount = deckCount;
        this.id = id;
        this.fill();
    }

    @Override
    public boolean isNeedShuffle() {
        return this.needShuffle;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    @Override
    public int getTotalCards() {
        return this.deckSample.size() * this.deckCount;
    }

    @Override
    public int getCardsLeft() {
        return Math.max(0, this.shoe.size() - 1);
    }

    @Override
    public AbstractCard pop() {
        var card = this.shoe.removeLast();
        LOG.trace(SHOE_EVENTS, "SHOE#{} Popping card {}", this.getId(), card);
        if (card == Card.CUTTING_CARD) {
            this.needShuffle = true;
            card = this.shoe.removeLast();
            LOG.trace(SHOE_EVENTS, "SHOE#{} It's cutting, next card {}", this.getId(), card);
        }

        return card;
    }

    @Override
    public int getDeckCount() {
        return this.deckCount;
    }

    @Override
    public Set<ICard> getDeckSample() {
        return this.deckSample;
    }

    @Override
    public void shuffle(IShuffler<AbstractCard> shuffler, Collection<AbstractCard> holder) {
        LOG.debug(SHOE_EVENTS, "SHOE#{} Begin shuffle shoe", this.getId());
        this.shoe.addAll(holder);
        shuffler.shuffle(this.shoe);
        var result = shuffler.validate(this.getDeckSample(), this.getDeckCount(), this.shoe);
        this.valid = result == IShuffler.VALID;
        this.needShuffle = false;

        if (!this.valid) {
            LOG.atError()
                .withMarker(SHOE_EVENTS)
                .withThrowable(new RuntimeException(result.getError()))
                .log("SHOE#{} Unable to validate shoe: {}", this.getId(), this);
        }
    }

    @Override
    public int getId() {
        return this.id;
    }

    private void fill() {
        LOG.trace(SHOE_EVENTS, "SHOE#{} Filling up shoe", this.getId());
        this.shoe.clear();
        this.needShuffle = true;
        this.valid = false;
        for (var i = 0; i < this.getDeckCount(); i++) {
            var deck = this.createDeck(i);
            LOG.trace(SHOE_EVENTS, "SHOE#{} Creating deck id#{}, contents: {}", this.getId(), i, deck);
            this.shoe.addAll(deck);
        }
    }

    private Set<AbstractCard> createDeck(int id) {
        return this.getDeckSample().stream()
                .map((card) -> new Card(card.getRank(), card.getSuit(), id))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
