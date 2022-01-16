package master.thesis.backend.errors;

import java.util.Optional;

public abstract class BaseError {

    private int lineNumber = -1;
    protected String containingClass = "";
    /**
     *
     * @return the name of the class this error is found in
     */
    public String getContainingClass() {
        return containingClass;
    }

    public void setContainingClass(String containingClass) {
        this.containingClass = containingClass;
    }

    /**
     *
     * @return The linenumber of this error. Returns -1 if no linenumber exists.
     */
    public int getLineNumber() {
        return this.lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     *
     * @return A string with a suggestion in code on how to fix this error.
     */
    public abstract Optional<String> getSuggestion();

    /**
     *
     * @return a tip of how you might fix this error
     */
    public abstract Optional<String> getTip();

    /**
     *
     * @return a string about what caused the error.
     * For example, an EqualsOperatorError is caused by using == instead of .equals on an object.
     */
    public abstract String getWhat();

    /**
     *
     * @return a string with more info about the error
     */
    public abstract Optional<String> getLink();


}
