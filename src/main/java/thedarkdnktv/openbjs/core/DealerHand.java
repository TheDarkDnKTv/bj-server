package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.game.Card;

public class DealerHand extends Hand implements IDealerHand {

    @Override
    public IHand getSplitHand() {
        return null;
    }

    @Override
    public IHand doSplit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Card getOpenCard() {
        if (!this.hand.isEmpty()) {
            return this.hand.get(0);
        }

        return null;
    }
}
