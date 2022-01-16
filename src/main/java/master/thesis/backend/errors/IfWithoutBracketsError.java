package master.thesis.backend.errors;

import java.util.Optional;

public class IfWithoutBracketsError extends BaseError {

    private String condition;
    private Object thenBranch;

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
    public String getWhat() {
        return "Java does not care about indentation like Python does! Java only cares about brackets, so you should always use brackets in an if-statement.";
    }

    @Override
    public Optional<String> getLink() {
        return Optional.of("https://master-thesis-frontend-prod.herokuapp.com/ifstatement");
    }

}
