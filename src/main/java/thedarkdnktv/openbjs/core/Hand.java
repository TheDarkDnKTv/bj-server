package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Decision;
import thedarkdnktv.openbjs.enums.Rank;

import java.util.*;

public class Hand implements IHand {

    protected final List<ICard> hand = new LinkedList<>();

    protected int score;
    protected boolean bj;
    protected boolean soft;
    protected boolean insured;
    protected boolean doubled;

    protected boolean isSplit;
    protected double bet;

    protected HandState state;
    protected IHand splitHand;

    public Hand() {
        this.state = HandState.EMPTY;
    }

    public Hand(boolean split) {
        this();
        this.isSplit = split;
    }

    @Override
    public HandState getState() {
        return this.state;
    }

    @Override
    public void setState(HandState state) {
        this.state = state;
    }

    @Override
    public void apply(ICard card) {
        this.hand.add(card);
        this.score += card.getRank().value;
        if (card.getRank() == Rank.ACE) {
            this.soft = true;
        }

        if (this.hand.size() == 2) {
            this.state = HandState.WAITING_TURN;
            if (checkIsBj()) {
                this.state = HandState.TURN_OVER;
                this.bj = true;
            }
        } else if (this.hand.size() > 2 && this.score >= BjUtil.MAX_SCORE) {
            this.state = HandState.TURN_OVER;
        }
    }

    @Override
    public double getBet() {
        return this.bet;
    }

    @Override
    public void setBet(double value) {
        this.bet = value;
    }

    @Override
    public int getScore() {
        return this.score;
    }

    @Override
    public boolean isBj() {
        return this.bj;
    }

    @Override
    public boolean isSoft() {
        return this.soft;
    }

    @Override
    public boolean isInsured() {
        return this.insured;
    }

    @Override
    public void setInsured() {
        this.insured = true;
    }

    @Override
    public boolean isDoubled() {
        return this.doubled;
    }

    @Override
    public void setDoubled() {
        this.doubled = true;
    }

    @Override
    public void reset() {
        this.hand.clear();
        this.score = 0;
        this.bj = false;
        this.soft = false;
        this.insured = false;
        this.doubled = false;
        this.splitHand = null;
        if (this.getState() != HandState.EMPTY) {
            this.setState(HandState.IDLE);
        }
    }

    @Override
    public IHand getSplitHand() {
        return this.splitHand;
    }

    @Override
    public IHand doSplit() {
        if (!this.performableDecisions().contains(Decision.SPLIT)) {
            throw new IllegalStateException("Can not perform split as it's forbidden");
        }

        var cards = new LinkedList<>(this.hand);
        this.hand.clear();
        this.score = 0;
        this.soft = false;
        this.splitHand = new Hand(true);

        this.apply(cards.pop());
        this.splitHand.apply(cards.pop());

        this.setState(HandState.DEALING);
        this.splitHand.setState(HandState.DEALING);

        return this.splitHand;
    }

    @Override
    public Set<Decision> performableDecisions() {
        return switch (this.getState()) {
            case WAITING_TURN, DECISION_REQUIRED -> {
                var result = new HashSet<Decision>();
                if (!this.checkIsBj()) {
                    result.add(Decision.STAND);
                    result.add(Decision.HIT);
                    if (this.hand.size() == 2 && !this.isSplit) {
                        if (this.getScore() <= BjUtil.DOUBLE_DOWN_SCORE) {
                            result.add(Decision.DOUBLE_DOWN);
                        }

                        if (this.getScore() == BjUtil.SPLIT_SCORE && this.isSoft()) {
                            result.add(Decision.SPLIT);
                        }
                    }
                }

                yield result;
            }
            default -> Collections.emptySet();
        };
    }

    protected boolean checkIsBj() {
        if (this.hand.size() == 2) {
            var first = this.hand.get(0).getRank();
            var second = this.hand.get(1).getRank();
            return (first == Rank.ACE && second.value == 10) ||
                    (second == Rank.ACE && first.value == 10);
        }

        return false;
    }
}
