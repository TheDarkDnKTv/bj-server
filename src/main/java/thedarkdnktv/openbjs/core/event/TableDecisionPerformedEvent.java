package thedarkdnktv.openbjs.core.event;

import thedarkdnktv.openbjs.core.IHand;
import thedarkdnktv.openbjs.enums.Decision;

public class TableDecisionPerformedEvent extends BaseTableHandEvent {

    public final Decision decision;
    public final IHand.HandState state;

    public TableDecisionPerformedEvent(int id, int handId, Decision decision, IHand.HandState state) {
        super(id, handId);
        this.decision = decision;
        this.state = state;
    }
}
