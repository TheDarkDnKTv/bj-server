package thedarkdnktv.openbjs.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thedarkdnktv.openbjs.core.IHand.*;
import thedarkdnktv.openbjs.core.event.TableCardDealtEvent;
import thedarkdnktv.openbjs.core.event.TableDecisionPerformedEvent;
import thedarkdnktv.openbjs.core.event.TableStateEvent;
import thedarkdnktv.openbjs.core.event.TableTurnEvent;
import thedarkdnktv.openbjs.enums.Decision;
import thedarkdnktv.openbjs.exception.DecisionNotPossibleException;
import thedarkdnktv.openbjs.util.Timer;

import java.time.Duration;
import java.util.LinkedList;

import static thedarkdnktv.openbjs.Logging.TABLE_EVENTS;

public class Table implements IGameTable<AbstractCard>, Identifiable {

    protected static final Logger LOG = LogManager.getLogger();

    protected static final Duration TIME_BETTING    = Duration.ofSeconds(5);
    protected static final Duration TIME_DEAL       = Duration.ofMillis(400);
    protected static final Duration TIME_DECISION   = Duration.ofSeconds(30);

    private final Timer timer = new Timer();

    private final int id;
    private final double minBet;
    private final double maxBet;
    private final IHand[] slots;
    private final IEventBus events;
    private final LinkedList<AbstractCard> holder;

    private State state;
    private IDealerHand dealer;
    private int activeSlot;
    private boolean dealerTurn;
    private boolean willShuffle;

    private IShuffler<AbstractCard> shuffler;
    private IShoe<AbstractCard> shoe;

    public Table(int id, int seats, double minBet, double maxBet, IEventBus bus) {
        this.id = id;
        if (seats <= 0) {
            throw new IllegalArgumentException("Seats count can not be less or equals 0");
        }

        this.minBet = minBet;
        this.maxBet = maxBet;
        this.slots = new IHand[seats];
        this.events = bus;
        this.holder = new LinkedList<>();
    }

    @Override
    public void init() {
        LOG.debug(TABLE_EVENTS, "TABLE#{} Init of table", this.getId());
        this.dealer = new DealerHand();
        for (var i = 0; i < this.slots.length; i++) {
            this.slots[i] = new Hand();
        }

        this.setState(State.WAITING_FOR_BETS);
        if (this.shoe.isNeedShuffle()) {
            this.shoe.shuffle(this.shuffler, this.holder);
        }
    }

    @Override
    public void update() {
//        LOG.trace(TABLE_EVENTS, "TABLE#{} Update loop", this.getId());
        switch (this.getState()) {
            case BETTING_TIME: {
                if (timer.tick() || this.isPlayersReady()) {
                    this.setDealing();
                    LOG.info(TABLE_EVENTS, "TABLE#{} Bets are closed", this.getId());
                }

                break;
            }
            case DEALING: {
                if (timer.tick()) {
                    if (this.dealerTurn) {
                        this.dealerTurn = false;
                        this.doDeal(this.dealer, -1);
                        if (this.dealer.getState() != HandState.DEALING) {
                            this.setInGame();
                            LOG.info(TABLE_EVENTS, "TABLE#{} Cards dealt, starting game", this.getId());
                        }
                    } else do {
                        var handId = this.getActiveSlot();
                        var hand = this.getSlot(handId);
                        var willDeal = hand.getState() == HandState.DEALING;
                        if (willDeal) {
                            this.doDeal(hand, handId);
                        }

                        this.dealerTurn = this.doSlotRotation();
                        if (willDeal) {
                            break;
                        }
                    } while (!this.dealerTurn);

                    if (!this.isWillShuffle() && this.shoe.isNeedShuffle()) {
                        LOG.debug(TABLE_EVENTS, "TABLE#{} Shoe shuffle scheduled", this.getId());
                        this.setWillShuffle(true);
                    }
                }

                break;
            }
            case IN_GAME: {
                if (!this.dealerTurn) {
                    var handId = this.getActiveSlot();
                    var hand = this.getSlot(handId);
                    switch (hand.getState()) {
                        case WAITING_TURN: {
                            hand.setState(HandState.DECISION_REQUIRED);
                            events.post(new TableTurnEvent(id, handId));
                            break;
                        }
                        case DECISION_REQUIRED: {
                            if (hand.getDecision() != null) {
                                this.performDecision(handId, hand);
                            }

                            if (!this.timer.tick()) {
                                break;
                            }

                            LOG.info(TABLE_EVENTS, "TABLE#{} Player decision timeout", this.getId());
                            hand.setState(HandState.TURN_OVER);
                        }
                        default: {
                            if (this.doSlotRotation()) {
                                this.dealerTurn = true;
                                this.timer.setTime(TIME_DEAL);
                            }

                            break;
                        }
                    }
                } else if (this.timer.tick()) { // Dealing dealer's hand
                    if (!this.dealer.isHiddenCardOpen()) {
                        this.dealer.setHiddenCardOpen();
                        if (this.isGameResultDefined()) {
                            this.dealer.setState(HandState.TURN_OVER);
                        } else if (this.dealer.getState() != HandState.TURN_OVER) {
                            events.post(new TableTurnEvent(id, -1));
                            this.dealer.setState(HandState.DECISION_REQUIRED);
                        }

                        break;
                    }

                    if (this.dealer.getState() == HandState.DECISION_REQUIRED) {
                        this.doDeal(this.dealer, -1);
                    } else if (this.dealer.getState() == HandState.TURN_OVER) {
                        this.setGameResolved();
                        LOG.info(TABLE_EVENTS, "TABLE#{} Game resolved", this.getId());
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
    public IDealerHand getDealerHand() {
        return this.dealer;
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
    public double getMinBet() {
        return this.minBet;
    }

    @Override
    public double getMaxBet() {
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
        if (slot != null && slot.getState() != HandState.EMPTY && this.canResultBeDefined(slot)) {
            return new GameResult(slot, this.dealer);
        }

        return null;
    }

    @Override
    public void setDecision(int slotIdx, Decision decision) throws DecisionNotPossibleException {
        var state = this.getState();
        if (state != State.DEALING && state != State.IN_GAME) {
            throw new DecisionNotPossibleException("Table state is invalid: " + state);
        }

        var hand = this.getSlot(slotIdx);
        if (hand == null) {
            throw new DecisionNotPossibleException("Hand is not exists");
        }

        var handState = hand.getState();
        if (handState != HandState.WAITING_TURN && handState != HandState.DECISION_REQUIRED) {
            throw new DecisionNotPossibleException("Hand state is invalid: " + handState);
        }

        var allowedDecisions = hand.performableDecisions();
        if (!allowedDecisions.contains(decision)) {
            throw new DecisionNotPossibleException("Decision is not allowed: " + decision)
                    .setAllowedDecisions(allowedDecisions);
        }

        LOG.debug(TABLE_EVENTS, "TABLE#{} Decision set for hand {}", this.getId(), hand);
        hand.setDecision(decision);
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
        this.events.post(new TableStateEvent(this.id, state));
        LOG.debug(TABLE_EVENTS, "TABLE#{} Setting state {}", this.getId(), state);
    }

    protected boolean isWillShuffle() {
        return this.willShuffle;
    }

    protected void setWillShuffle(boolean willShuffle) {
        this.willShuffle = willShuffle;
    }

    protected boolean doSlotRotation() {
        if (++this.activeSlot >= this.getSlotCount()) {
            this.activeSlot = 0;
            return true;
        }

        return false;
    }

    protected void doDeal(IHand hand, int handId) {
        var card = this.shoe.pop();
        this.holder.add(card);
        LOG.debug(TABLE_EVENTS, "TABLE#{} Dealing card {} for hand {}", this.getId(), card, hand);
        hand.apply(card);
        var hidden = state == State.DEALING &&
                hand instanceof IDealerHand dealerHand &&
                dealerHand.getOpenCard() != card;
        events.post(new TableCardDealtEvent(id, handId, card, hidden));
    }

    protected void performDecision(int handId, IHand hand) {
        final var decision = hand.getDecision();
        switch (decision) {
            case HIT, DOUBLE_DOWN -> {
                this.doDeal(hand, handId);
            }
            case SPLIT -> {
                // TODO
            }
            case STAND -> {
                hand.setState(HandState.TURN_OVER);
            }
        }

        hand.setDecision(null);
        events.post(new TableDecisionPerformedEvent(id, handId, decision, hand.getState()));
    }

    protected void setBettingTime() {
        this.setState(State.BETTING_TIME);
        this.timer.setTime(TIME_BETTING);
    }

    protected void setInGame() {
        LOG.debug(TABLE_EVENTS, "TABLE#{} Setting IN_GAME", this.getId());
        this.setState(State.IN_GAME);
        this.activeSlot = 0;
        this.dealerTurn = false;
        for (var hand : this.slots) {
            if (hand.getState() == HandState.HAS_NO_BET) {
                hand.setState(HandState.IDLE); // TODO kick idle feature
            }
        }

        this.timer.setTime(TIME_DECISION);
    }

    protected void setDealing() {
        boolean hasBets = false;
        for (var slot : this.slots) {
            if (slot.getState() == HandState.HAS_BET) {
                slot.setState(HandState.DEALING);
                hasBets = true;
            } else {
//                slot.setState(HandState.HAS_NO_BET); // TODO
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
        this.dealerTurn = false;
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
        return slot.isTooMany() || this.dealer.getState() == HandState.TURN_OVER ||
                (slot.isBj() && (slot == this.dealer || this.dealer.getOpenCard().getRank().value < 10));
    }

    protected boolean isGameResultDefined() {
        for (var slot : this.slots) {
            if (slot.getState() == HandState.TURN_OVER) {
                if (!slot.isTooMany() && !slot.isBj()) {
                    return false;
                }
            }
        }

        return this.dealer.getState() == HandState.TURN_OVER;
    }

    protected boolean isPlayersReady() {
        for (var hand : this.slots) {
            if (hand.getState() != HandState.EMPTY && !hand.isReady()) {
                return false;
            }
        }

        return true;
    }
}
