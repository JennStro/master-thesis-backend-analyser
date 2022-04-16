package master.thesis.backend.analyser;

import master.thesis.backend.errors.BaseError;

import java.util.ArrayList;

public interface AnalyserConfiguration {

    /**
     *
     * @return The errors the analyser should ignore.
     * The list contains the name of the error when calling {@link BaseError#getName()}.
     */
    ArrayList<String> getErrorsToIgnore();

    ArrayList<String> getErrorsToIgnoreForClass(String className);

}
