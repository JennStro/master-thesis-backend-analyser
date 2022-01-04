package errors;

public class EqualsOperatorError extends BaseError {

    private String objectOne;
    private String objectTwo;

    public EqualsOperatorError(int offset, int length) {
        super(offset, length);
    }

    public String getSuggestion() {
        return "You should try " + this.objectOne + ".equals(" + this.objectTwo + ")";
    }

    public void setObjectOne(String objectOne) {
        this.objectOne = objectOne;
    }

    public void setObjectTwo(String objectTwo) {
        this.objectTwo = objectTwo;
    }

    @Override
    public boolean hasSuggestion() {
        return this.objectOne != null && this.objectTwo != null;
    }

    @Override
    public String getWhat() {
        return "You are using \"==\" to compare objects.";
    }
}