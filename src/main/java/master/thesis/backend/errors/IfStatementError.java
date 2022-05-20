package master.thesis.backend.errors;

import java.util.Optional;

public class IfStatementError extends BaseError {

    String condition;
    String thenBranch;

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setThenBranch(String thenBranch) {
        this.thenBranch = thenBranch;
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
        return "IfStatementError";
    }
}
