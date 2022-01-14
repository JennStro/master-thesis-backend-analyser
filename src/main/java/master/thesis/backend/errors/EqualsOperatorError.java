package master.thesis.backend.errors;

import java.util.Optional;

public class EqualsOperatorError extends BaseError {

    private String objectOne;
    private String objectTwo;
    private boolean isNegated = false;

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
            return "In Python you could use \"!=\" to compare objects, but in Java we use \"!=\" to see if two objects are the same instance.";
        }
        return "In Python you could use \"==\" to compare objects, but in Java we use \"==\" to see if two objects are the same instance.";
    }

    @Override
    public Optional<String> getLink() {
        return Optional.of("https://master-thesis-frontend-prod.herokuapp.com/equalsoperator");
    }
}
