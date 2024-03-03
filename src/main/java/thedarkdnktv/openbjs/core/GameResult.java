package thedarkdnktv.openbjs.core;

public class GameResult {

    private final IHand mine;
    private final IHand their;

    private double payout = -1;

    public GameResult(IHand mine, IHand their) {
        this.mine = mine;
        this.their = their;
    }

    private void calculatePayout() {
        this.payout = 0.0D;

        // Too many
        if (mine.getScore() > BjUtil.MAX_SCORE) {
            return;
        }

        if (their.isBj()) {
            // push
            if (mine.isBj()) {
                this.payout += 1.0D;
            }

            if (mine.isInsured()) {
                this.payout += 1.0D;
            }

            return;
        }

        if (mine.getScore() > their.getScore()) {
            this.payout = 2.0D;
            if (mine.isDoubled()) {
                this.payout += 1.0D;
            }
        } else if (mine.getScore() == their.getScore()) {
            this.payout = 1.0D; // push
        }
    }

    public boolean isWin() {
        return this.getPayout() > 0D;
    }

    public double getPayout() {
        if (this.payout < 0) {
            this.calculatePayout();
        }

        return this.payout;
    }
}