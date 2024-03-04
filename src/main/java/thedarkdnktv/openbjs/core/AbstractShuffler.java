package thedarkdnktv.openbjs.core;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.ListMultimap;
import thedarkdnktv.openbjs.enums.Rank;
import thedarkdnktv.openbjs.enums.Suit;

import java.util.Collection;
import java.util.Objects;

public abstract class AbstractShuffler<T extends ICard> implements IShuffler<T> {

    protected ListMultimap<ICard, T> getCardMapped(Collection<T> shoe) {
        ListMultimap<ICard, T> map = MultimapBuilder.hashKeys()
                .arrayListValues()
                .build();

        for (var value : shoe) {
            if (value.getSuit() == null || value.getRank() == null) {
                continue;
            }

            var key = new CardKey(value);
            map.put(key, value);
        }

        return map;
    }

    /**
     * This is a wrapper, it's needed to make sure equals() and hashCode() will consider only Rank and Suit
     */
    protected static class CardKey implements ICard {

        private final ICard nested;

        protected CardKey(ICard nested) {
            this.nested = nested;
        }

        @Override
        public Suit getSuit() {
            return this.nested.getSuit();
        }

        @Override
        public Rank getRank() {
            return this.nested.getRank();
        }

        @Override
        public String represent() {
            return "KEY[" + this.nested.represent() + "]";
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
}
