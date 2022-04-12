package master.thesis.backend.errors;

import java.util.Optional;

public class BinaryExprError extends BaseError {

    String leftOperand;
    String operator;
    String rightOperand;

    public void setLeftOperand(String leftOperand) {
        this.leftOperand = leftOperand;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setRightOperand(String rightOperand) {
        this.rightOperand = rightOperand;
    }


    @Override
    public Optional<String> getSuggestion() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getTip() {
        return Optional.empty();
    }

    @Override
    public String getCauseOfError() {
        return null;
    }

    @Override
    public Optional<String> getMoreInfoLink() {
        return Optional.empty();
    }

    @Override
    public String getName() {
        return "BinaryExprError";
    }
}
