package master.thesis.backend.errors;

import java.util.Optional;

public class IfWithoutBracketsError extends BaseError {

    private String condition;
    private String thenBranch;

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setThenBranch(String thenBranch) {
        this.thenBranch = thenBranch;
    }

    @Override
    public Optional<String> getSuggestion() {
        if (this.condition != null && this.thenBranch != null) {
            return Optional.of("if ("+ this.condition + ")" + " {"
                    + this.thenBranch + "..."
                    + "}");
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getTip() {
        return Optional.empty();
    }

    @Override
    public String getCauseOfError() {
        return "You have an if without brackets! Java does not care about indentation like Python does, but brackets.";
    }

    @Override
    public Optional<String> getMoreInfoLink() {
        return Optional.of("https://master-thesis-frontend-prod.herokuapp.com/ifstatement");
    }

    @Override
    public String getName() {
        return "IfWithoutBracketsError";
    }
}
