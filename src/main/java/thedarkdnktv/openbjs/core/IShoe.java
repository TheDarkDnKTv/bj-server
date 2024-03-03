package thedarkdnktv.openbjs.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Set;

public interface IShoe {

    Logger LOG = LogManager.getLogger();

    boolean isNeedShuffle();

    boolean isValid();

    int getDeckCount();

    Set<ICard> getDeckSample();

    int getTotalCards();

    int getCardsLeft();

    ICard pop();

    void shuffle(IShuffler shuffler, Collection<ICard> holder);
}
