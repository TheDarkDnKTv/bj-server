package thedarkdnktv.openbjs.exception;

import thedarkdnktv.openbjs.enums.Decision;

import java.util.HashSet;
import java.util.Set;

public class DecisionNotPossibleException extends Exception {

    private Set<Decision> allowedDecisions = new HashSet<>();

    public DecisionNotPossibleException(String message) {
        super(message);
    }

    public Set<Decision> getAllowedDecisions() {
        return this.allowedDecisions;
    }

    public DecisionNotPossibleException setAllowedDecisions(Set<Decision> allowedDecisions) {
        this.allowedDecisions.addAll(allowedDecisions);
        return this;
    }
}
