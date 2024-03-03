package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Decision;
import thedarkdnktv.openbjs.exception.DecisionNotPossibleException;

public interface IGameTable<T extends ICard> {

    void init();

    void update();

    State getState();

    GameResult getResult(int slot);

    IHand getCurrentSlot();

    int getActiveSlot();

    IHand getSlot(int slot);

    void setDecision(int slot, Decision decision) throws DecisionNotPossibleException;

    int getSlotCount();

    int getMinBet();

    int getMaxBet();

    void setBet(int slot, double bet);

    void setShoe(IShoe<T> shoe);

    void setShuffler(IShuffler<T> shuffler);

    enum State {
        WAITING_FOR_BETS,
        BETTING_TIME,
        DEALING,
        IN_GAME,
        GAME_RESOLVED,
        DISABLED
    }
}
