package thedarkdnktv.openbjs.core;

import java.util.Collection;

public interface IShuffler {

    ValidationResult VALID = new ValidationResult(null);

    /**
     * @param shoe to check
     * @return ValidationResult with error if so
     * @throws thedarkdnktv.openbjs.exception.ShoeNotValidException in case if shoe is invalid
     */
    ValidationResult validate(Collection<ICard> shoe);

    /**
     * @param shoe to shuffle
     */
    void shuffle(Collection<ICard> shoe);

    record ValidationResult(String error) {

        public boolean isError() {
            return this.error() != null;
        }
    }
}
