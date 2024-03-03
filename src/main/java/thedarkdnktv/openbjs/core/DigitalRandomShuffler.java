package thedarkdnktv.openbjs.core;

import java.util.Collection;
import java.util.Set;

public class DigitalRandomShuffler extends AbstractShuffler<AbstractCard> {

    @Override
    public ValidationResult validate(Set<ICard> sample, int deckCount, Collection<AbstractCard> shoe) {
        var size = sample.size() * deckCount + 1; // with cutting card
        if (size != shoe.size()) {
            return new ValidationResult("Card count does not match");
        }

        var map = this.getCardMapped(shoe);
        for (var sampleCard : sample) {
            var cards = map.get(sampleCard);
            if (cards.size() != deckCount) {
                return new ValidationResult("Count of " + sampleCard.represent() + " does not match deck count")
                        .setSubset(cards);
            }
        }

        return VALID;
    }

    @Override
    public void shuffle(Collection<AbstractCard> shoe) {
        // TODO
    }
}
