package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Rank;
import thedarkdnktv.openbjs.enums.Suit;

import java.util.Objects;

public abstract class AbstractCard implements ICard {

    @Override
    public abstract Suit getSuit();

    @Override
    public abstract Rank getRank();

    @Override
    public String represent() {
        return this.getSuit().symbol + this.getRank().denomination;
    }

    @Override
    public final int compareTo(ICard o) {
        return ICard.super.compareTo(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getRank(), this.getSuit());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ICard other) {
            return this.getRank() == other.getRank() &&
                    this.getSuit() == other.getSuit();
        }

        return false;
    }
}
