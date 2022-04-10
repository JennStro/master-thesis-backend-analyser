package master.thesis.backend.visitor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;

public class AnnotationVisitor extends VoidVisitorAdapter<Void> {

    private ArrayList<String> annotations = new ArrayList<>();

    @Override
    public void visit(MarkerAnnotationExpr annotationExpr, Void arg) {
        super.visit(annotationExpr, arg);
        annotations.add(annotationExpr.toString());
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        super.visit(declaration, arg);
    }

    public ArrayList<String> getAnnotations() {
        return this.annotations;
    }


}
