package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Rank;
import thedarkdnktv.openbjs.enums.Suit;

import java.util.Comparator;
import java.util.Objects;

public abstract class AbstractCard implements ICard, Identifiable {

    @Override
    public abstract Suit getSuit();

    @Override
    public abstract Rank getRank();

    @Override
    public int compareTo(ICard o) {
        if (o instanceof AbstractCard o1) {
            return Comparator.comparing(AbstractCard::getRank)
                    .thenComparing(AbstractCard::getSuit)
                    .thenComparing(AbstractCard::getId)
                    .compare(this, o1);
        }

        return ICard.super.compareTo(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getRank(), this.getSuit(), this.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractCard other) {
            return this.getRank() == other.getRank() &&
                    this.getSuit() == other.getSuit() &&
                    this.getId() == other.getId();
        }

        return false;
    }
}
