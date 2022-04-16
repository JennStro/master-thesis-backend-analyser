package master.thesis.backend.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class AnnotationVisitor extends VoidVisitorAdapter<Void> {

    private ArrayList<String> annotations = new ArrayList<>();
    private HashMap<String, ArrayList<String>> annotationsPerClass = new HashMap<>();

    @Override
    public void visit(MarkerAnnotationExpr annotationExpr, Void arg) {
        super.visit(annotationExpr, arg);
        annotations.add(annotationExpr.toString());
        Optional<ClassOrInterfaceDeclaration> maybeAncestor = annotationExpr.findAncestor(ClassOrInterfaceDeclaration.class);

        if (maybeAncestor.isPresent()) {
            String currentClassName = maybeAncestor.get().getNameAsString();
            ArrayList<String> annotationsFromCurrentClass = this.annotationsPerClass.get(currentClassName);
            if (annotationsFromCurrentClass == null) {
                annotationsFromCurrentClass = new ArrayList<>();
            }
            annotationsFromCurrentClass.add(annotationExpr.toString());
            this.annotationsPerClass.put(currentClassName, annotationsFromCurrentClass);
        }
        System.out.println("VISITOR: Class or interface: " + maybeAncestor.get().getNameAsString());
    }

    public ArrayList<String> getAnnotations() {
        return this.annotations;
    }

    public ArrayList<String> getAnnotationsForClass(String className) {
        return annotationsPerClass.get(className);
    }
}
