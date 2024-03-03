package thedarkdnktv.openbjs;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public final class Logging {

    public static final Marker GAME_EVENTS = MarkerManager.getMarker("GAME_EVENTS");
    public static final Marker TABLE_EVENTS = MarkerManager.getMarker("TABLE_EVENTS");
    public static final Marker SHOE_EVENTS = MarkerManager.getMarker("SHOE_EVENTS");

    static {
        TABLE_EVENTS.addParents(GAME_EVENTS);
        SHOE_EVENTS.addParents(GAME_EVENTS);
    }
}
