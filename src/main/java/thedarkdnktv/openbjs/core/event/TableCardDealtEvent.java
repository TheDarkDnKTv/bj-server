package thedarkdnktv.openbjs.core.event;

import thedarkdnktv.openbjs.core.ICard;

public class TableCardDealtEvent extends BaseTableHandEvent {

    public final ICard card;
    public final boolean hidden;

    public TableCardDealtEvent(int id, int handId, ICard card, boolean hidden) {
        super(id, handId);
        this.card = card;
        this.hidden = hidden;
    }
}
