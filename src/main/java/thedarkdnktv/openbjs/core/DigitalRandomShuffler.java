package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.game.Card;

import java.util.*;

public class DigitalRandomShuffler extends AbstractShuffler<AbstractCard> {

    private final Random random = new Random(System.currentTimeMillis());

    @Override
    public ValidationResult validate(Set<ICard> sample, int deckCount, List<AbstractCard> shoe) {
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
    public void shuffle(List<AbstractCard> shoe) {
        Collections.shuffle(shoe, this.random);
        var idx = shoe.size() / 2;
        var multiplier = (int) Math.ceil(idx * 0.05D);
        idx += this.random.nextInt(multiplier) - Math.max(1, multiplier / 2);
        shoe.add(idx, Card.CUTTING_CARD);
    }
}
