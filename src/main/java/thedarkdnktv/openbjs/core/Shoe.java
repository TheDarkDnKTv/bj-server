package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.game.Card;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

public final class Shoe implements IShoe<AbstractCard> {

    private final LinkedList<AbstractCard> shoe = new LinkedList<>();
    private final Set<ICard> deckSample;
    private final int deckCount;

    private boolean needShuffle;
    private boolean valid;

    public Shoe(int deckCount, Set<ICard> deck) {
        this.deckSample = Collections.unmodifiableSet(deck);
        this.deckCount = deckCount;
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
        if (card == Card.CUTTING_CARD) {
            this.needShuffle = true;
            card = this.shoe.removeLast();
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
        this.shoe.addAll(holder);
        shuffler.shuffle(this.shoe);
        var result = shuffler.validate(this.getDeckSample(), this.getDeckCount(), this.shoe);
        this.valid = result == IShuffler.VALID;
        this.needShuffle = false;

        if (!this.valid) {
            LOG.error("Unable to validate shoe: {}", this, new RuntimeException(result.getError()));
        }
    }

    private void fill() {
        this.shoe.clear();
        this.needShuffle = true;
        this.valid = false;
        for (var i = 0; i < this.getDeckCount(); i++) {
            this.shoe.addAll(this.createDeck(i));
        }
    }

    private Set<AbstractCard> createDeck(int id) {
        return this.getDeckSample().stream()
                .map((card) -> new Card(card.getRank(), card.getSuit(), id))
                .collect(Collectors.toSet());
    }
}
