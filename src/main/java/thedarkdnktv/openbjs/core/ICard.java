package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Rank;
import thedarkdnktv.openbjs.enums.Suit;

import java.util.Comparator;

public interface ICard extends Comparable<ICard> {

    Suit getSuit();

    Rank getRank();

    String represent();

    @Override
    default int compareTo(ICard o) {
        return Comparator.comparing(ICard::getRank)
                .thenComparing(ICard::getSuit)
                .compare(this, o);
    }
}
