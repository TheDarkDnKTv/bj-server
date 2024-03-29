package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Decision;
import thedarkdnktv.openbjs.enums.Rank;

import java.util.*;

public class Hand implements IHand {

    protected final LinkedList<ICard> hand = new LinkedList<>();

    protected int score;
    protected boolean bj;
    protected boolean soft;
    protected boolean insured;
    protected boolean doubled;

    protected boolean isSplit;
    protected boolean ready;
    protected double bet;

    protected HandState state;
    protected Decision decision;
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
    public int getTotalScore() {
        var score = this.getScore();
        if (score <= BjUtil.SOFT_SCORE && this.isSoft()) {
            score += 10;
        }

        return score;
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
    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    @Override
    public Decision getDecision() {
        return this.decision;
    }

    @Override
    public void reset() {
        this.hand.clear();
        this.score = 0;
        this.bj = false;
        this.soft = false;
        this.insured = false;
        this.doubled = false;
        this.ready = false;
        this.splitHand = null;
        this.decision = null;
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
                    if (this.hand.size() == 2 && !this.hasDoneSplit()) {
                        result.add(Decision.DOUBLE_DOWN);
                        if (this.hasPairs()) {
                            result.add(Decision.SPLIT);
                        }
                    }
                }

                yield result;
            }
            default -> Collections.emptySet();
        };
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    @Override
    public void setReady() {
        if (this.getState() == HandState.HAS_BET) {
            this.ready = true;
        }
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

    protected boolean hasDoneSplit() {
        return this.isSplit || this.getSplitHand() != null;
    }

    protected boolean hasPairs() {
        if (this.hand.size() >= 2) {
            return this.hand.get(0).getRank() == this.hand.get(1).getRank();
        }

        return false;
    }

    @Override
    public String toString() {
        var cards = new StringBuilder();
        for (var card : this.hand) {
            cards.append(card.toString());
            cards.append(' ');
        }

        var len = cards.length() - 1;
        if (len > 0) {
            cards.deleteCharAt(len);
        }

        var score = this.getScore();
        String scoreStr;
        if (this.isBj()) {
            scoreStr = "BJ";
        } else if (score <= BjUtil.SOFT_SCORE && this.isSoft()) {
            scoreStr = score + "/" + (score + 10);
        } else {
            scoreStr = Integer.toString(score);
        }

        return String.format("{ %s } - %s", cards, scoreStr);
    }
}
