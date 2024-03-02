package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Decision;
import thedarkdnktv.openbjs.game.Card;

import java.util.Set;

public interface IHand {

    HandState getState();

    void setState(HandState state);

    void apply(Card card);

    int getScore();

    boolean isBj();

    boolean isSoft();

    boolean isInsured();

    boolean isDoubled();

    void setInsured();

    void setDoubled();

    IHand getSplitHand();

    IHand doSplit();

    Set<Decision> performableDecisions();

    int getBet();

    void reset();

    enum HandState {
        EMPTY,
        BETTING,
        HAS_BET,
        HAS_NO_BET,
        DEALING,
        WAITING_TURN,
        DECISION_REQUIRED,
        TURN_OVER
    }
}