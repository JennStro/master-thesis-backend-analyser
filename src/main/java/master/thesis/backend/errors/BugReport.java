package master.thesis.backend.errors;

import java.util.ArrayList;
import java.util.Optional;

/**
 * The bugreport. Used by {@link master.thesis.backend.visitor.BugFinderVisitor} to collect bugs during analysis.
 * If a parseerror or exception during analysis occurs, it will be attached to this report and available through
 * {@link #getException()}.
 */
public class BugReport {

    private ArrayList<BaseError> bugs = new ArrayList<>();
    private Throwable exceptionFromJavaParser;
    private String className;

    public ArrayList<BaseError> getBugs() {
        return bugs;
    }

    public void addBug(BaseError error) {
        this.bugs.add(error);
    }

    public void attach(Throwable exceptionFromJavaParser) {
        this.exceptionFromJavaParser = exceptionFromJavaParser;
    }

    /**
     *
     * @return empty if no exception. The exception if attached during analysis or parsing.
     */
    public Optional<Throwable> getException() {
        if (exceptionFromJavaParser == null) {
            return Optional.empty();
        }
        return Optional.of(exceptionFromJavaParser);
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {return this.className;}
}
