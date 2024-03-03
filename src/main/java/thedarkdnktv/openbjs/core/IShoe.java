package thedarkdnktv.openbjs.core;

import java.util.Collection;
import java.util.Set;

public interface IShoe<T extends ICard> {

    boolean isNeedShuffle();

    boolean isValid();

    int getDeckCount();

    Set<ICard> getDeckSample();

    int getTotalCards();

    int getCardsLeft();

    T pop();

    void shuffle(IShuffler<T> shuffler, Collection<T> holder);
}
