package thedarkdnktv.openbjs.core.event;

import thedarkdnktv.openbjs.core.ICard;

public class TableCardDealtEvent extends BaseTableHandEvent {

    public final ICard card;

    public TableCardDealtEvent(int id, int handId, ICard card) {
        super(id, handId);
        this.card = card;
    }
}
