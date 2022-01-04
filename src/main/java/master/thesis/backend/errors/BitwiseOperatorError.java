package master.thesis.backend.errors;

public class BitwiseOperatorError extends BaseError {

    private String leftOperand;
    private String operator;
    private String rightOperand;

    public BitwiseOperatorError(int offset, int length) {
        super(offset, length);
    }

    @Override
    public String getSuggestion() {
        return "You should try: \n \n" + this.leftOperand + " " + convertBitwiseOperatorToConditionalOperator(this.operator) + " " + this.rightOperand;
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
    public boolean hasSuggestion() {
        return this.leftOperand != null && this.operator != null && this.rightOperand != null;
    }
    @Override
    public String getWhat() {
        return "You are using the bitwiseoperator (& or |) as a logical operator.";
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