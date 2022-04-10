package master.thesis.backend.visitor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class AnnotationFinder extends VoidVisitorAdapter<Void> {

    private ArrayList<String> annotations = new ArrayList<>();
    private HashMap<String, String> fromAnnotationToName = new HashMap<>();

    public AnnotationFinder() {
        super();
        fromAnnotationToName.put("@BitwiseOperationAllowed", "BitwiseOperatorError");
        fromAnnotationToName.put("@EqualsOperatorOnObjectAllowed", "EqualsOperatorError");
        fromAnnotationToName.put("@IfWithoutBracketsAllowed", "IfWithoutBracketsError");
        fromAnnotationToName.put("@IntegerDivisionAllowed", "IntegerDivisionError");
        fromAnnotationToName.put("@NoEqualsMethod", "MissingEqualsMethodError");
        fromAnnotationToName.put("@IfStatementWithSemicolonAllowed", "SemiColonAfterIfError");
    }

    @Override
    public void visit(MarkerAnnotationExpr annotationExpr, Void arg) {
        super.visit(annotationExpr, arg);
        annotations.add(annotationExpr.toString());
        System.out.println(annotationExpr);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        super.visit(declaration, arg);
    }

    public ArrayList<String> getAnnotations() {
        return this.annotations;
    }

    public ArrayList<String> errorsToIgnore() {
        ArrayList<String> errorsToIgnore = new ArrayList<>();
        for (String annotation : this.annotations) {
            errorsToIgnore.add(fromAnnotationToName.get(annotation));
        }
        return errorsToIgnore;
    }
}
