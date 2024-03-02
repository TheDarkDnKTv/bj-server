package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.core.IHand.*;

public class Table implements IGameTable {

    private final int minBet;
    private final int maxBet;
    private final IHand[] slots;

    private State state = State.WAITING_FOR_BETS;
    private IDealerHand dealer;
    private int activeSlot;

    public Table(int seats, int minBet, int maxBet) {
        if (seats <= 0) {
            throw new IllegalArgumentException("Seats count can not be less or equals 0");
        }

        this.minBet = minBet;
        this.maxBet = maxBet;
        this.slots = new IHand[seats];
    }

    @Override
    public void init() {
        this.dealer = new DealerHand();
        for (var i = 0; i < this.slots.length; i++) {
            this.slots[i] = new Hand();
        }
    }

    @Override
    public void update() {

    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public IHand getCurrentSlot() {
        return this.slots[this.activeSlot];
    }

    @Override
    public IHand getSlot(int slot) {
        if (slot >= 0 && slot < this.slots.length) {
            return this.slots[slot];
        }

        return null;
    }

    @Override
    public int getActiveSlot() {
        return this.activeSlot;
    }

    @Override
    public int getSlotCount() {
        return this.slots.length;
    }

    @Override
    public int getMinBet() {
        return this.minBet;
    }

    @Override
    public int getMaxBet() {
        return this.maxBet;
    }

    @Override
    public GameResult getResult(int slotIdx) {
        var slot = this.getSlot(slotIdx);
        if (slot != null && this.canResultBeDefined(slot)) {
            return new GameResult(slot, this.dealer);
        }

        return null;
    }

    protected boolean canResultBeDefined(IHand slot) {
        return slot.getScore() > BjUtil.MAX_SCORE ||
                this.dealer.getState() == HandState.TURN_OVER ||
                (slot.isBj() && this.dealer.getOpenCard().getRank().value < 10);
    }
}
