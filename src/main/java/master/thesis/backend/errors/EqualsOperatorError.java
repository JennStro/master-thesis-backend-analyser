package master.thesis.backend.errors;

import java.util.Optional;

public class EqualsOperatorError extends BaseError {

    private String objectOne;
    private String objectTwo;
    private boolean isNegated = false;

    public EqualsOperatorError(int offset, int length) {
        super(offset, length);
    }

    @Override
    public Optional<String> getSuggestion() {
        if (this.objectOne != null && this.objectTwo != null) {
            if (isNegated) {
                return Optional.of("You should try !" + this.objectOne + ".equals(" + this.objectTwo + ")");
            }
            return Optional.of("You should try " + this.objectOne + ".equals(" + this.objectTwo + ")");
        }
        return Optional.empty();
    }

    public void withNegatedOperator() {
        this.isNegated = true;
    }

    public void setObjectOne(String objectOne) {
        this.objectOne = objectOne;
    }

    public void setObjectTwo(String objectTwo) {
        this.objectTwo = objectTwo;
    }

    @Override
    public String getWhat() {
        if (isNegated) {
            return "You are using \"!=\" to compare objects.";
        }
        return "You are using \"==\" to compare objects.";
    }
}