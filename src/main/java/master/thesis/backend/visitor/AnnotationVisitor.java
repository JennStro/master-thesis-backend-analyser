package master.thesis.backend.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class AnnotationVisitor extends VoidVisitorAdapter<Void> {

    private ArrayList<String> annotations = new ArrayList<>();
    private HashMap<String, ArrayList<String>> annotationsPerClass = new HashMap<>();

    @Override
    public void visit(MarkerAnnotationExpr annotationExpr, Void arg) {
        super.visit(annotationExpr, arg);
        annotations.add(annotationExpr.toString());
    }

    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        super.visit(declaration, arg);
        List<Node> children = declaration.getChildNodes();

        for (Node child : children) {
            if (child instanceof MarkerAnnotationExpr) {
                ArrayList<String> annotationsFromCurrentClass = this.annotationsPerClass.get(declaration.getNameAsString());
                if (annotationsFromCurrentClass == null) {
                    annotationsFromCurrentClass = new ArrayList<>();
                }
                annotationsFromCurrentClass.add(child.toString());
                this.annotationsPerClass.put(declaration.getNameAsString(), annotationsFromCurrentClass);
            }
        }
    }

    public ArrayList<String> getAnnotations() {
        return this.annotations;
    }

    public ArrayList<String> getAnnotationsForClass(String className) {
        if (annotationsPerClass.get(className) == null) {
            return new ArrayList<>();
        }
        return annotationsPerClass.get(className);
    }
}
