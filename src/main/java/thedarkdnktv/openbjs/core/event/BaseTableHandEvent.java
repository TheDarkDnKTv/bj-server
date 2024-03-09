package thedarkdnktv.openbjs.core.event;

public class BaseTableHandEvent extends BaseTableEvent {
    public final int handId;

    public BaseTableHandEvent(int id, int handId) {
        super(id);
        this.handId = handId;
    }
}
