package thedarkdnktv.openbjs.core;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.ListMultimap;
import thedarkdnktv.openbjs.enums.Rank;
import thedarkdnktv.openbjs.enums.Suit;

import java.util.Collection;

public abstract class AbstractShuffler<T extends ICard> implements IShuffler<T> {

    protected ListMultimap<ICard, T> getCardMapped(Collection<T> shoe) {
        ListMultimap<ICard, T> map = MultimapBuilder.hashKeys()
                .arrayListValues()
                .build();

        for (var value : shoe) {
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
    }
}
