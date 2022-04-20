package master.thesis.backend.analyser;

import master.thesis.backend.errors.BaseError;

import java.util.ArrayList;

public interface AnalyserConfiguration {

    /**
     *
     * @return The errors the analyser should ignore for the chosen class.
     * The list contains the name of the error when calling {@link BaseError#getName()}.
     */
    ArrayList<String> getErrorsToIgnoreForClass(String className);

}
