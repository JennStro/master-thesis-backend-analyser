package master.thesis.backend.errors;

import master.thesis.backend.annotations.AnnotationNames;

import java.util.Optional;

public class EqualsOperatorError extends BaseError {

    private String objectOne;
    private String objectTwo;
    private boolean isNegated = false;
    private boolean isArray = false;

    @Override
    public Optional<String> getSuggestion() {
        if (this.objectOne != null && this.objectTwo != null) {
            if (isArray) {
                if (isNegated) {
                    return Optional.of("!Arrays.equals(" + this.objectOne + ", " + this.objectTwo + ")");
                }
                return Optional.of("Arrays.equals(" + this.objectOne + ", " + this.objectTwo + ")");
            }
            if (isNegated) {
                return Optional.of("!" + this.objectOne + ".equals(" + this.objectTwo + ")");
            }
            return Optional.of(this.objectOne + ".equals(" + this.objectTwo + ")");
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getTip() {
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

    public void setArraysSuggestion() {
        this.isArray = true;
    }

    @Override
    public String getCauseOfError() {
        if (isNegated) {
            return "You are using \"!=\" to compare objects! In Python you could do this, but in Java we use the equals method.";
        }
        return "You are using \"==\" to compare objects! In Python you could do this, but in Java we use the equals method.";
    }

    @Override
    public Optional<String> getMoreInfoLink() {
        return Optional.of("https://master-thesis-frontend-prod.herokuapp.com/equalsoperator");
    }

    @Override
    public String annotationName() {
        return AnnotationNames.EQUALS_OPERATOR_ERROR;
    }
}
