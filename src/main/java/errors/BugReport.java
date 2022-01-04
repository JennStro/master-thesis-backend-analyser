package errors;

import java.util.ArrayList;

public class BugReport {

    private int numberOfBugs;
    private ArrayList<BaseError> bugs = new ArrayList<>();
    private String fileName;

    public int getNumberOfBugs() {
        return numberOfBugs;
    }

    public ArrayList<BaseError> getBugs() {
        return bugs;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setNumberOfBugs(int numberOfBugs) {
        this.numberOfBugs = numberOfBugs;
    }

    public void addBug(BaseError error) {
        this.bugs.add(error);
    }
}
