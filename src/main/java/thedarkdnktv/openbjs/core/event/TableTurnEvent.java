package thedarkdnktv.openbjs.core.event;

public class TableTurnEvent extends BaseTableHandEvent {

    public TableTurnEvent(int id, int handId) {
        super(id, handId);
    }

    public boolean isDealer() {
        return this.handId < 0;
    }
}
