package thedarkdnktv.openbjs.core;

import java.util.*;

public interface IShuffler<T extends ICard> {

    ValidationResult VALID = new ValidationResult(null);

    /**
     * Will validate a shoe with a cutting card in order to check all cards are present
     * @param shoe to check
     * @return ValidationResult with error if so
     */
    ValidationResult validate(Set<ICard> sample, int deckCount, List<T> shoe);

    /**
     * @param shoe to shuffle
     */
    void shuffle(List<T> shoe);

    class ValidationResult {

        private final String error;
        private Set<ICard> subset;

        public ValidationResult(String error) {
            this.error = error;
        }

        public boolean isError() {
            return this.getError() != null;
        }

        public String getError() {
            return this.error;
        }

        public Set<ICard> getSubset() {
            return Objects.requireNonNullElse(this.subset, Collections.emptySet());
        }

        public ValidationResult setSubset(Collection<? extends ICard> subset) {
            this.subset = new HashSet<>(subset);
            return this;
        }
    }
}
