package master.thesis.backend.errors;

import java.util.Optional;

public class IfWithoutBracketsError extends BaseError {

    private String ifStatement;
    private Object thenBranch;

    public IfWithoutBracketsError(int offset, int length) {
        super(offset, length);
    }

    public void setIfStatementWithoutBody(String ifStatement) {
        this.ifStatement = "if ("+ifStatement + ")";
    }

    public void setThenBranch(String thenBranch) {
        this.thenBranch = thenBranch;
    }

    @Override
    public Optional<String> getSuggestion() {
        if (this.ifStatement != null && this.thenBranch != null) {
            return Optional.of("You should enclose the body in brackets: \n"
                    + this.ifStatement + " { \n"
                    + "    " +  this.thenBranch + "\n"
                    + "}");
        }
        return Optional.empty();
    }

    @Override
    public String getWhat() {
        return "You should use brackets after if!";
    }

}