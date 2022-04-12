package master.thesis.backend.errors;

import java.util.Optional;

public class IntegerDivisionError extends BinaryExprError {

    @Override
    public Optional<String> getSuggestion() {
        if (this.leftOperand != null && this.rightOperand != null) {
            return Optional.of("(double)" + this.leftOperand + "/" + "(double)" + this.rightOperand);
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
    public String getName() {
        return "IntegerDivisionError";
    }

}
