package master.thesis.backend.errors;

import java.util.Optional;

public class SemiColonAfterIfError extends BaseError {


    private String condition;

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public Optional<String> getSuggestion() {
        if (this.condition != null) {
            return Optional.of("if (" + this.condition + ") { }");
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getTip() {
        return Optional.empty();
    }

    @Override
    public String getWhat() {
        return "You have a semicolon (;) after an if-statement!";
    }

    @Override
    public Optional<String> getLink() {
        return Optional.empty();
    }

}
