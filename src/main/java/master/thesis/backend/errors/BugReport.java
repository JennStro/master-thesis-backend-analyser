package master.thesis.backend.errors;

import com.github.javaparser.ParseProblemException;

import java.util.ArrayList;
import java.util.Optional;

public class BugReport {

    private ArrayList<BaseError> bugs = new ArrayList<>();
    private String className;
    private ParseProblemException e;

    public int getNumberOfBugs() {
        return bugs.size();
    }

    public ArrayList<BaseError> getBugs() {
        return bugs;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void addBug(BaseError error) {
        this.bugs.add(error);
    }

    public void attach(ParseProblemException e) {
        this.e = e;
    }

    public Optional<ParseProblemException> getException() {
        if (e == null) {
            return Optional.empty();
        }
        return Optional.of(e);
    }
}
