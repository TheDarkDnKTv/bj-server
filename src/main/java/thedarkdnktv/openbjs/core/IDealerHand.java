package thedarkdnktv.openbjs.core;

public interface IDealerHand extends IHand {

    ICard getOpenCard();

    void setHiddenCardOpen();

    boolean isHiddenCardOpen();
}
