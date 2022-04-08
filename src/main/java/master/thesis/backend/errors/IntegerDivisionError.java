package master.thesis.backend.errors;

import master.thesis.backend.annotations.AnnotationNames;

import java.util.Optional;

public class IntegerDivisionError extends BaseError {

    public void setLeftInteger(String leftInteger) {
        this.leftInteger = leftInteger;
    }

    public void setRightInteger(String rightInteger) {
        this.rightInteger = rightInteger;
    }

    private String leftInteger;
    private String rightInteger;

    @Override
    public Optional<String> getSuggestion() {
        if (leftInteger != null && rightInteger != null) {
            return Optional.of("(double)" + leftInteger + "/" + "(double)" + rightInteger);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getTip() {
        return Optional.empty();
    }

    @Override
    public String getCauseOfError() {
        return "You are doing an integer division! In Python, you could divide two integers and get a decimal as a result. In Java we need to change the integers to decimals before we divide to get the same result.";
    }

    @Override
    public Optional<String> getMoreInfoLink() {
        return Optional.of("https://master-thesis-frontend-prod.herokuapp.com/integerdivision");
    }

    @Override
    public String annotationName() {
        return AnnotationNames.INTEGER_DIVISION_ERROR;
    }
}
