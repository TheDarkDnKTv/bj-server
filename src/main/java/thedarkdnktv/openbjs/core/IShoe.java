package thedarkdnktv.openbjs.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Set;

public interface IShoe<T extends ICard> {

    Logger LOG = LogManager.getLogger();

    boolean isNeedShuffle();

    boolean isValid();

    int getDeckCount();

    Set<ICard> getDeckSample();

    int getTotalCards();

    int getCardsLeft();

    T pop();

    void shuffle(IShuffler<T> shuffler, Collection<T> holder);
}
