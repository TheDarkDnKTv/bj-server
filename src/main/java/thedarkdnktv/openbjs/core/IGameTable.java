package thedarkdnktv.openbjs.core;

public interface IGameTable {

    void init();

    void update();

    State getState();

    GameResult getResult(int slot);

    IHand getCurrentSlot();

    int getActiveSlot();

    IHand getSlot(int slot);

    int getSlotCount();

    int getMinBet();

    int getMaxBet();

    enum State {
        WAITING_FOR_BETS,
        BETTING_TIME,
        IN_GAME,
        GAME_RESOLVED,
        DISABLED
    }
}
