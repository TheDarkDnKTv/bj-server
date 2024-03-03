package thedarkdnktv.openbjs.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thedarkdnktv.openbjs.core.IHand.*;
import thedarkdnktv.openbjs.util.Timer;

import java.time.Duration;
import java.util.LinkedList;

import static thedarkdnktv.openbjs.Logging.TABLE_EVENTS;

public class Table implements IGameTable<AbstractCard>, Identifiable {

    protected static final Logger LOG = LogManager.getLogger();

    protected static final Duration TIME_BETTING    = Duration.ofSeconds(30);
    protected static final Duration TIME_DEAL       = Duration.ofMillis(400);
    protected static final Duration TIME_DECISION   = Duration.ofSeconds(15);

    private final Timer timer = new Timer();

    private final int id;
    private final int minBet;
    private final int maxBet;
    private final IHand[] slots;
    private final LinkedList<AbstractCard> holder;

    private State state = State.WAITING_FOR_BETS;
    private IDealerHand dealer;
    private int activeSlot;
    private boolean isDealerTurn;
    private boolean willShuffle;

    private IShuffler<AbstractCard> shuffler;
    private IShoe<AbstractCard> shoe;

    public Table(int id, int seats, int minBet, int maxBet) {
        this.id = id;
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
        LOG.debug(TABLE_EVENTS, "[TABLE#{}] Init of table", this.getId());
        this.dealer = new DealerHand();
        for (var i = 0; i < this.slots.length; i++) {
            this.slots[i] = new Hand();
        }
    }

    @Override
    public void update() {
        LOG.debug(TABLE_EVENTS, "[TABLE#{}] Update loop", this.getId());
        switch (this.getState()) {
            case BETTING_TIME: {
                if (timer.tick()) {
                    this.setDealing();
                    LOG.info(TABLE_EVENTS, "[TABLE#{}] Bets are closed", this.getId());
                }

                break;
            }
            case DEALING: {
                if (timer.tick()) {
                    if (this.isDealerTurn) {
                        this.isDealerTurn = false;
                        this.doDeal(this.dealer);
                        if (this.dealer.getState() != HandState.DEALING) {
                            this.setInGame();
                            LOG.info(TABLE_EVENTS, "[TABLE#{}] Cards dealt, starting game", this.getId());
                        }
                    } else do {
                        if (this.doSlotRotation()) {
                            this.isDealerTurn = true;
                            break;
                        }

                        var hand = this.getCurrentSlot();
                        if (hand.getState() == HandState.DEALING) {
                            this.doDeal(hand);
                            break;
                        }
                    } while (true);

                    if (!this.isWillShuffle() && this.shoe.isNeedShuffle()) {
                        LOG.debug(TABLE_EVENTS, "[TABLE#{}] Shoe shuffle scheduled", this.getId());
                        this.setWillShuffle(true);
                    }
                }

                break;
            }
            case IN_GAME: {
                if (!this.isDealerTurn) {
                    var hand = this.getCurrentSlot();
                    switch (hand.getState()) {
                        case WAITING_TURN: {
                            hand.setState(HandState.DECISION_REQUIRED);
                            break;
                        }
                        case DECISION_REQUIRED: {
                            if (!this.timer.tick()) {
                                break;
                            }

                            LOG.info(TABLE_EVENTS, "[TABLE#{}] Player decision timeout", this.getId());
                            hand.setState(HandState.TURN_OVER);
                        }
                        default: {
                            if (this.doSlotRotation()) {
                                this.isDealerTurn = true;
                                this.timer.setTime(TIME_DEAL);
                            }

                            break;
                        }
                    }
                } else if (this.timer.tick()) { // Dealing dealer's hand
                    if (!this.dealer.isHiddenCardOpen()) {
                        this.dealer.setHiddenCardOpen();
                        break;
                    }

                    if (this.dealer.getState() == HandState.DECISION_REQUIRED) {
                        this.doDeal(this.dealer);
                    } else if (this.dealer.getState() == HandState.TURN_OVER) {
                        this.setGameResolved();
                        LOG.info(TABLE_EVENTS, "[TABLE#{}] Game resolved", this.getId());
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
    public void setShuffler(IShuffler<AbstractCard> shuffler) {
        this.shuffler = shuffler;
    }

    @Override
    public void setShoe(IShoe<AbstractCard> shoe) {
        this.shoe = shoe;
    }

    @Override
    public int getId() {
        return this.id;
    }

    protected void setState(State state) {
        this.state = state;
        LOG.debug(TABLE_EVENTS, "[TABLE#{}] Setting state {}", this.getId(), state);
    }

    protected boolean isWillShuffle() {
        return this.willShuffle;
    }

    protected void setWillShuffle(boolean willShuffle) {
        this.willShuffle = willShuffle;
    }

    protected boolean doSlotRotation() {
        if (this.activeSlot++ >= this.getSlotCount()) {
            this.activeSlot = 0;
            return true;
        }

        return false;
    }

    protected void doDeal(IHand hand) {
        var card = this.shoe.pop();
        this.holder.add(card);
        LOG.trace(TABLE_EVENTS, "[TABLE#{}] Dealing card {} for hand {}", this.getId(), card, hand);
        hand.apply(card);
    }

    protected void setBettingTime() {
        this.setState(State.BETTING_TIME);
        this.timer.setTime(TIME_BETTING);
    }

    protected void setInGame() {
        LOG.debug(TABLE_EVENTS, "[TABLE#{}] Setting IN_GAME", this.getId());
        this.setState(State.IN_GAME);
        this.activeSlot = 0;
        this.isDealerTurn = false;
        this.timer.setTime(TIME_DECISION);
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
            this.setState(State.DEALING);
            this.timer.setTime(TIME_DEAL);
            this.dealer.setState(HandState.DEALING);
            this.activeSlot = 0;
        } else {
            this.setWaiting();
        }
    }

    protected void setWaiting() {
        this.setState(State.WAITING_FOR_BETS);
        this.activeSlot = 0;
        this.isDealerTurn = false;
        this.dealer.reset();
        for (var slot : slots) {
            slot.reset();
        }
    }

    protected void setGameResolved() {
        this.setState(State.GAME_RESOLVED);
        this.activeSlot = 0;
    }

    protected boolean canResultBeDefined(IHand slot) {
        return slot.getScore() > BjUtil.MAX_SCORE ||
                this.dealer.getState() == HandState.TURN_OVER ||
                (slot.isBj() && (slot == this.dealer || this.dealer.getOpenCard().getRank().value < 10));
    }
}
