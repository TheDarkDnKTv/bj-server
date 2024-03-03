package thedarkdnktv.openbjs.core;

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
