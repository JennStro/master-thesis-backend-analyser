package master.thesis.backend.adapter;

import com.github.javaparser.ast.CompilationUnit;
import master.thesis.backend.analyser.AnalyserConfiguration;
import master.thesis.backend.errors.*;
import master.thesis.backend.visitor.AnnotationVisitor;

import java.util.ArrayList;
import java.util.HashMap;

public class AnnotationsAdapter implements AnalyserConfiguration {

    private HashMap<String, String> fromAnnotationToName = new HashMap<>();
    private AnnotationVisitor visitor;

    public AnnotationsAdapter(CompilationUnit compilationUnit) {
        fromAnnotationToName.put("@BitwiseOperationAllowed", new BitwiseOperatorError().getName());
        fromAnnotationToName.put("@EqualsOperatorOnObjectAllowed", new EqualsOperatorError().getName());
        fromAnnotationToName.put("@IfWithoutBracketsAllowed", new IfWithoutBracketsError().getName());
        fromAnnotationToName.put("@IntegerDivisionAllowed", new IntegerDivisionError().getName());
        fromAnnotationToName.put("@NoEqualsMethod", new MissingEqualsMethodError().getName());
        fromAnnotationToName.put("@IfStatementWithSemicolonAllowed", new SemiColonAfterIfError().getName());
        this.visitor = new AnnotationVisitor();
        visitor.visit(compilationUnit, null);
    }

    @Override
    public ArrayList<String> getErrorsToIgnoreForClass(String className) {
        ArrayList<String> errorsToIgnore = new ArrayList<>();
        ArrayList<String> annotations = visitor.getAnnotationsForClass(className);
        for (String annotation : annotations) {
            errorsToIgnore.add(fromAnnotationToName.get(annotation));
        }
        return errorsToIgnore;
    }
}
