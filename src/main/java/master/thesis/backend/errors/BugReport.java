package master.thesis.backend.errors;

import java.util.ArrayList;

public class BugReport {

    private ArrayList<BaseError> bugs = new ArrayList<>();
    private String className;

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
}
