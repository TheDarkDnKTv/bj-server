package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Decision;
import thedarkdnktv.openbjs.game.Card;

import java.util.Set;

public interface IHand {

    HandState getState();

    void setState(HandState state);

    void apply(ICard card);

    int getScore();

    boolean isBj();

    boolean isSoft();

    boolean isInsured();

    boolean isDoubled();

    void setInsured();

    void setDoubled();

    IHand getSplitHand();

    IHand doSplit();

    void setDecision(Decision decision);

    Decision getDecision();

    Set<Decision> performableDecisions();

    double getBet();

    void setBet(double value);

    boolean isReady();

    void setReady();

    void reset();

    enum HandState {
        EMPTY,
        IDLE,
        HAS_BET,
        HAS_NO_BET,
        DEALING,
        WAITING_TURN,
        DECISION_REQUIRED,
        TURN_OVER
    }
}