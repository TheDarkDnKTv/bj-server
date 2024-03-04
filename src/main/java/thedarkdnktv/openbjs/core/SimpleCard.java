package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Rank;
import thedarkdnktv.openbjs.enums.Suit;

import java.util.Objects;
import java.util.function.Consumer;

public record SimpleCard(Rank rank, Suit suit) implements ICard {

    public static void createFromRank(Rank rank, Consumer<ICard> consumer) {
        for (var suit : Suit.values()) {
            consumer.accept(new SimpleCard(rank, suit));
        }
    }

    @Override
    public Suit getSuit() {
        return suit;
    }

    @Override
    public Rank getRank() {
        return rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getRank(), this.getSuit());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ICard card) {
            return this.getRank() == card.getRank() &&
                    this.getSuit() == card.getSuit();
        }

        return false;
    }
}
