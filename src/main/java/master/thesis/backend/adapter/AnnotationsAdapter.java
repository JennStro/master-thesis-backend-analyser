package master.thesis.backend.adapter;

import java.util.ArrayList;
import java.util.HashMap;

public class AnnotationsAdapter {

    private HashMap<String, String> fromAnnotationToName = new HashMap<>();

    public AnnotationsAdapter() {
        fromAnnotationToName.put("@BitwiseOperationAllowed", "BitwiseOperatorError");
        fromAnnotationToName.put("@EqualsOperatorOnObjectAllowed", "EqualsOperatorError");
        fromAnnotationToName.put("@IfWithoutBracketsAllowed", "IfWithoutBracketsError");
        fromAnnotationToName.put("@IntegerDivisionAllowed", "IntegerDivisionError");
        fromAnnotationToName.put("@NoEqualsMethod", "MissingEqualsMethodError");
        fromAnnotationToName.put("@IfStatementWithSemicolonAllowed", "SemiColonAfterIfError");
    }

    public ArrayList<String> getErrorsToIgnoreAsName(ArrayList<String> annotations) {
        ArrayList<String> errorsToIgnore = new ArrayList<>();
        for (String annotation : annotations) {
            errorsToIgnore.add(fromAnnotationToName.get(annotation));
        }
        return errorsToIgnore;
    }

}
