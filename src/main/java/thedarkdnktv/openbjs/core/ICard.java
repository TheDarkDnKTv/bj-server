package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Rank;
import thedarkdnktv.openbjs.enums.Suit;

import java.util.Comparator;

public interface ICard extends Comparable<ICard> {

    Suit getSuit();

    Rank getRank();

    String represent();

    default Comparator<ICard> getComparator() {
        return Comparator.comparing(ICard::getRank)
                .thenComparing(ICard::getSuit);
    }

    @Override
    default int compareTo(ICard o) {
        return this.getComparator().compare(this, o);
    }
}
