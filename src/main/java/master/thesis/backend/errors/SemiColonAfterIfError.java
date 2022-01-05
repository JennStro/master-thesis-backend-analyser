package master.thesis.backend.errors;

import java.util.Optional;

public class SemiColonAfterIfError extends BaseError {


    private String condition;

    public SemiColonAfterIfError(int offset, int length) {
        super(offset, length);
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public Optional<String> getSuggestion() {
        if (this.condition != null) {
            return Optional.of("You should try \n \n if (" + this.condition + ") {\n 	// ...your code here... \n}");
        }
        return Optional.empty();
    }

    @Override
    public String getWhat() {
        return "You have a semicolon (;) after an if-statement.";
    }

}