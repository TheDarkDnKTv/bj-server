package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Rank;
import thedarkdnktv.openbjs.enums.Suit;

import java.util.Comparator;

public interface ICard extends Comparable<ICard> {

    Suit getSuit();

    Rank getRank();

    default String represent() {
        return this.getSuit().symbol + this.getRank().denomination;
    }

    @Override
    default int compareTo(ICard o) {
        return Comparator.comparing(ICard::getRank)
                .thenComparing(ICard::getSuit)
                .compare(this, o);
    }
}
