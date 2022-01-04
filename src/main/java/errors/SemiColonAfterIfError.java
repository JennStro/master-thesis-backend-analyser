package errors;

public class SemiColonAfterIfError extends BaseError {


    private String condition;

    public SemiColonAfterIfError(int offset, int length) {
        super(offset, length);
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getSuggestion() {
        return "You should try \n \n if (" + this.condition + ") {\n 	// ...your code here... \n}";
    }

    @Override
    public boolean hasSuggestion() {
        return this.condition != null;
    }

    @Override
    public String getWhat() {
        return "You have a semicolon (;) after an if-statement.";
    }

}