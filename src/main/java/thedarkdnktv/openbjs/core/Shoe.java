package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.game.Card;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

public final class Shoe implements IShoe {

    private final LinkedList<ICard> shoe = new LinkedList<>();
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
        return this.shoe.size();
    }

    @Override
    public ICard pop() {
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
    public void shuffle(IShuffler shuffler, Collection<ICard> holder) {
        this.shoe.addAll(holder);
        shuffler.shuffle(this.shoe);
        var result = shuffler.validate(this.shoe);
        this.valid = result == IShuffler.VALID;
        this.needShuffle = false;

        if (!this.valid) {
            LOG.error("Unable to validate shoe: {}", this, new RuntimeException(result.error()));
        }
    }

    private void fill() {
        // TODO
    }
}
