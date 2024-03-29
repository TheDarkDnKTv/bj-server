package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Decision;

public class DealerHand extends Hand implements IDealerHand {

    private boolean hiddenCardOpen;

    @Override
    public IHand getSplitHand() {
        return null;
    }

    @Override
    public IHand doSplit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        super.reset();
        this.hiddenCardOpen = false;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void apply(ICard card) {
        super.apply(card);
        if (this.hand.size() >= 2 && this.getTotalScore() > BjUtil.DEALER_DRAW_SCORE) {
            this.setState(HandState.TURN_OVER);
        }
    }

    @Override
    public void setDecision(Decision decision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICard getOpenCard() {
        if (!this.hand.isEmpty()) {
            return this.hand.get(0);
        }

        return null;
    }

    @Override
    public void setHiddenCardOpen() {
        this.hiddenCardOpen = true;
    }

    @Override
    public boolean isHiddenCardOpen() {
        return this.hiddenCardOpen;
    }
}
