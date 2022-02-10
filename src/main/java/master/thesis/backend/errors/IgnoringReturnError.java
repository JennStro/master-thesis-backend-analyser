package master.thesis.backend.errors;


import java.util.Optional;

public class IgnoringReturnError extends BaseError {

    private String returnType;
    private String methodCall;

    @Override
    public Optional<String> getSuggestion() {
        if (this.returnType != null && this.methodCall != null) {
            return Optional.of(this.returnType + " variableName = " + this.methodCall + ";");
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getTip() {
        return Optional.empty();
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public void setMethodCall(String methodCall) {
        this.methodCall = methodCall;
    }

    @Override
    public String getWhat() {
        return "You are ignoring a return value.";
    }

    @Override
    public Optional<String> getLink() {
        return Optional.empty();
    }

    @Override
    public String annotationName() {
        return "@IgnoreReturnValueAllowed";
    }
}
