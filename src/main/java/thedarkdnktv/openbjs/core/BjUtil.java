package thedarkdnktv.openbjs.core;

import thedarkdnktv.openbjs.enums.Rank;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BjUtil {

    public static final int MAX_SCORE = 21;
    public static final int DOUBLE_DOWN_SCORE = 11;
    public static final int SOFT_SCORE = 11;
    public static final int SPLIT_SCORE = 2;
    public static final int DEALER_DRAW_SCORE = 16;

    public static final Set<ICard> DECK_52_CARDS;

    static {
        var set = Arrays.stream(Rank.values())
                .mapMulti(SimpleCard::createFromRank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        DECK_52_CARDS = Collections.unmodifiableSet(set);
    }
}
