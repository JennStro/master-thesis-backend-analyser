package master.thesis.backend.errors;

import java.util.Optional;

public abstract class BaseError {

    private int offsetStart = -1;
    private int offsetEnd = -1;
    private int lineNumber = -1;
    protected String containingClass = "";

    public int getOffset() {
        return this.offsetStart;
    }

    public int getLength() {
        return this.offsetEnd;
    }

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

    public void setOffsetStart(int offsetStart) {
        this.offsetStart = offsetStart;
    }

    public void setOffsetEnd(int offsetEnd) {
        this.offsetEnd = offsetEnd;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
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