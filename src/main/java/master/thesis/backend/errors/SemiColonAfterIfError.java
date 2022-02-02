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
            return Optional.of("to remove the semicolon after the if-condition: if (" + this.condition + ") { // The rest of your code }");
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getTip() {
        return Optional.empty();
    }

    @Override
    public String getWhat() {
        return "You have a semicolon (;) after an if-statement! In Python we use a colon (:) here, but you don't need this after if-statement in Java!";
    }

    @Override
    public Optional<String> getLink() {
        return Optional.of("https://master-thesis-frontend-prod.herokuapp.com/semicolon");
    }

}
