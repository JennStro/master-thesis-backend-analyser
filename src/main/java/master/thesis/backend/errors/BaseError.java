package master.thesis.backend.errors;

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
     * @return True if this error has a suggestion available.
     */
    public abstract boolean hasSuggestion();

    /**
     *
     * @return A string with a suggestion on how to fix this error.
     *
     * Check {@link #hasSuggestion()} first.
     */
    public abstract String getSuggestion();

    /**
     *
     * @return a string about what caused the error.
     * For example, an EqualsOperatorError is caused by using == instead of .equals on an object.
     */
    public abstract String getWhat();


}