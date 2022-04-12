package master.thesis.backend.errors;

import java.util.ArrayList;
import java.util.Optional;

public class BugReport {

    private ArrayList<BaseError> bugs = new ArrayList<>();
    private Throwable exceptionFromJavaParser;

    public ArrayList<BaseError> getBugs() {
        return bugs;
    }

    public void addBug(BaseError error) {
        this.bugs.add(error);
    }

    public void attach(Throwable exceptionFromJavaParser) {
        this.exceptionFromJavaParser = exceptionFromJavaParser;
    }

    public Optional<Throwable> getException() {
        if (exceptionFromJavaParser == null) {
            return Optional.empty();
        }
        return Optional.of(exceptionFromJavaParser);
    }
}
