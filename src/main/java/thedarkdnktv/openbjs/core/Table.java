package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.core.IHand.*;
import thedarkdnktv.openbjs.util.Timer;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Optional;

public class Table implements IGameTable {

    protected static final Duration TIME_BETTING    = Duration.ofSeconds(30);
    protected static final Duration TIME_DEAL       = Duration.ofMillis(400);

    private final Timer timer = new Timer();

    private final int minBet;
    private final int maxBet;
    private final IHand[] slots;
    private final LinkedList<ICard> holder;

    private State state = State.WAITING_FOR_BETS;
    private IDealerHand dealer;
    private int activeSlot;
    private boolean willShuffle;

    private IShuffler shuffler;
    private IShoe shoe;

    public Table(int seats, int minBet, int maxBet) {
        if (seats <= 0) {
            throw new IllegalArgumentException("Seats count can not be less or equals 0");
        }

        this.minBet = minBet;
        this.maxBet = maxBet;
        this.slots = new IHand[seats];
        this.holder = new LinkedList<>();
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
        switch (this.getState()) {
            case BETTING_TIME: {
                if (timer.tick()) {
                    this.setDealing();
                }

                break;
            }
            case DEALING: {
                if (timer.tick()) {
                    var opt = this.dealNext();
                    if (opt.isPresent()) {
                        var hand = opt.get();
                        var card = this.shoe.pop();
                        this.holder.add(card);
                        hand.apply(card);
                    } else {
                        this.setInGame();
                    }


                    if (!this.isWillShuffle() && this.shoe.isNeedShuffle()) {
                        this.setWillShuffle(true);
                    }
                }

                break;
            }
            case GAME_RESOLVED: {
                if (this.isWillShuffle()) {
                    this.shoe.shuffle(this.shuffler, this.holder);
                    this.setWillShuffle(false);
                }

                if (timer.tick()) {
                    this.setWaiting();
                }

                break;
            }
        }
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
    public void setBet(int slotIdx, double bet) {
        var slot = this.getSlot(slotIdx);
        if (slot == null) {
            throw new IllegalArgumentException("No slot found on idx: " + slotIdx);
        }

        var state = this.getState();
        switch (state) {
            default:
                throw new IllegalStateException("Can not bet at state: " + state);
            case WAITING_FOR_BETS:
                this.setBettingTime();
            case BETTING_TIME:
                slot.setBet(bet);
                slot.setState(HandState.HAS_BET);
        }
    }

    @Override
    public GameResult getResult(int slotIdx) {
        var slot = this.getSlot(slotIdx);
        if (slot != null && this.canResultBeDefined(slot)) {
            return new GameResult(slot, this.dealer);
        }

        return null;
    }

    @Override
    public void setShuffler(IShuffler shuffler) {
        this.shuffler = shuffler;
    }

    @Override
    public void setShoe(IShoe shoe) {
        this.shoe = shoe;
    }

    protected boolean isWillShuffle() {
        return this.willShuffle;
    }

    protected void setWillShuffle(boolean willShuffle) {
        this.willShuffle = willShuffle;
    }

    protected Optional<IHand> dealNext() {
        if (this.activeSlot++ >= this.getSlotCount()) {
            this.activeSlot = 0;
        }

        var hand = this.slots[this.activeSlot];
        if (hand.getState() == HandState.DEALING) {
            return Optional.of(hand);
        }

        return Optional.empty();
    }

    protected void setBettingTime() {
        this.state = State.BETTING_TIME;
        this.timer.setTime(TIME_BETTING);
    }

    protected void setInGame() {
        this.activeSlot = 0;
    }

    protected void setDealing() {
        boolean hasBets = false;
        for (var slot : this.slots) {
            if (slot.getState() == HandState.HAS_BET) {
                slot.setState(HandState.DEALING);
                hasBets = true;
            } else {
                slot.setState(HandState.HAS_NO_BET);
            }
        }

        if (hasBets) {
            this.state = State.DEALING;
            this.timer.setTime(TIME_DEAL);
            this.activeSlot = 0;
        } else {
            this.setWaiting();
        }
    }

    protected void setWaiting() {
        this.state = State.WAITING_FOR_BETS;
        this.activeSlot = -1;
        this.dealer.reset();
        for (var slot : slots) {
            slot.reset();
        }
    }

    protected boolean canResultBeDefined(IHand slot) {
        return slot.getScore() > BjUtil.MAX_SCORE ||
                this.dealer.getState() == HandState.TURN_OVER ||
                (slot.isBj() && this.dealer.getOpenCard().getRank().value < 10);
    }
}
