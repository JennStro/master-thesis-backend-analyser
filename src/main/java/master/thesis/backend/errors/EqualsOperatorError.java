package master.thesis.backend.errors;

import java.util.Optional;

public class EqualsOperatorError extends BinaryExprError {

    private boolean isArray = false;

    @Override
    public Optional<String> getSuggestion() {
        if (this.leftOperand != null && this.rightOperand != null) {
            if (isArray) {
                if (this.operator.equals("!=")) {
                    return Optional.of("!Arrays.equals(" + this.leftOperand + ", " + this.rightOperand + ")");
                }
                return Optional.of("Arrays.equals(" + this.leftOperand + ", " + this.rightOperand + ")");
            }
            if (this.operator.equals("!=")) {
                return Optional.of("!" + this.leftOperand + ".equals(" + this.rightOperand + ")");
            }
            return Optional.of(this.leftOperand + ".equals(" + this.rightOperand + ")");
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getTip() {
        return Optional.empty();
    }

    public void setArraysSuggestion() {
        this.isArray = true;
    }

    @Override
    public String getCauseOfError() {
        if (this.operator.equals("!=")) {
            return "You are using \"!=\" to compare objects! In Python you could do this, but in Java we use the equals method.";
        }
        return "You are using \"==\" to compare objects! In Python you could do this, but in Java we use the equals method.";
    }

    @Override
    public Optional<String> getMoreInfoLink() {
        return Optional.of("https://master-thesis-frontend-prod.herokuapp.com/equalsoperator");
    }

    @Override
    public String getName() {
        return "EqualsOperatorError";
    }

}
