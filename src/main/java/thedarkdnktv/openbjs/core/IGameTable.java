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

    void setBet(int slot, double bet);

    void setShoe(IShoe shoe);

    void setShuffler(IShuffler shuffler);

    enum State {
        WAITING_FOR_BETS,
        BETTING_TIME,
        DEALING,
        IN_GAME,
        GAME_RESOLVED,
        DISABLED
    }
}
