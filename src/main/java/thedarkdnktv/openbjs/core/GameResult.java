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
        if (mine.isTooMany()) {
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

        if (mine.isBj()) {
            payout = 2.5D;
            return;
        }

        if (mine.getTotalScore() > their.getTotalScore() || their.isTooMany()) {
            this.payout = 2.0D;
            if (mine.isDoubled()) {
                this.payout += 2.0D;
            }
        } else if (mine.getTotalScore() == their.getTotalScore()) {
            this.payout = 1.0D; // push
        }
    }

    public boolean isWin() {
        return this.getPayout() > 1.0D;
    }

    public boolean isPush() {
        return this.getPayout() == 1.0D;
    }

    public double getPayout() {
        if (this.payout < 0) {
            this.calculatePayout();
        }

        return this.payout;
    }

    public double getBet() {
        return this.mine.getBet();
    }
}