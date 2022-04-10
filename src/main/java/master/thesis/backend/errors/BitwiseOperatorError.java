package master.thesis.backend.errors;

import java.util.Optional;

public class BitwiseOperatorError extends BaseError {

    private String leftOperand;
    private String operator;
    private String rightOperand;

    @Override
    public Optional<String> getSuggestion() {
        if (this.leftOperand != null && this.operator != null && this.rightOperand != null) {
            return Optional.of("replacing " + this.operator + " with " + convertBitwiseOperatorToConditionalOperator(this.operator) + ": " + this.leftOperand + " " + convertBitwiseOperatorToConditionalOperator(this.operator) + " " + this.rightOperand);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getTip() {
        return Optional.empty();
    }

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
    public String getCauseOfError() {
        if (this.operator != null) {
            return "You are using the bitwise operator ("+ this.operator +"). In Python we use \"and\" and \"or\" as boolean operators, but in Java we use \"&&\" (and) and \"||\" (or)!";
        }
        return "You are using the bitwise operator (\"&\" and \"|\"). In Python we use \"and\" and \"or\" as boolean operators, but in Java we use \"&&\" (and) and \"||\" (or)!";
    }

    @Override
    public Optional<String> getMoreInfoLink() {
        return Optional.of("https://master-thesis-frontend-prod.herokuapp.com/bitwiseoperator");
    }

    @Override
    public String getName() {
        return "BitwiseOperatorError";
    }

    private String convertBitwiseOperatorToConditionalOperator(String operator) {
        if ("&".equals(operator)) {
            return "&&";
        }
        if ("|".equals(operator)) {
            return "||";
        }
        return "";
    }
}
