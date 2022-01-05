package master.thesis.backend.errors;

import java.util.Optional;

public abstract class BaseError {

    private final int offset;
    private final int length;

    public BaseError(int offset, int length) {
        this.offset = offset;
        this.length = length;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getLength() {
        return this.length;
    }

    /**
     *
     * @return A string with a suggestion on how to fix this error.
     */
    public abstract Optional<String> getSuggestion();

    /**
     *
     * @return a string about what caused the error.
     * For example, an EqualsOperatorError is caused by using == instead of .equals on an object.
     */
    public abstract String getWhat();


}