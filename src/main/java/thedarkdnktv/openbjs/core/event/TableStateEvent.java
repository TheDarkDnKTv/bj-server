package thedarkdnktv.openbjs.core.event;

import thedarkdnktv.openbjs.core.IGameTable;

public class TableStateEvent extends BaseTableEvent {

    public final IGameTable.State state;

    public TableStateEvent(int id, IGameTable.State state) {
        super(id);
        this.state = state;
    }
}
