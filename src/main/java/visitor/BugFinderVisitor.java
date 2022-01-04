package visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import errors.BugReport;
import errors.MissingEqualsMethodError;

import java.util.List;

public class BugFinderVisitor extends VoidVisitorAdapter<Void> {

    private BugReport report = new BugReport();

    @Override
    public void visit(MethodDeclaration declaration, Void arg) {
         super.visit(declaration, arg);
         System.out.println("Method Name Printed: " + declaration.getName());

    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        super.visit(declaration, arg);
        report.setFileName(declaration.getNameAsString());
        List<Node> children = declaration.getChildNodes();
        System.out.println(children);
        boolean classHasEqualsMethod = false;
        for (Node child : children) {
            if (child instanceof MethodDeclaration) {
                MethodDeclaration equalsMethodCandidate = (MethodDeclaration) child;
                if (equalsMethodCandidate.getNameAsString().equals("equals")) {
                    classHasEqualsMethod = true;
                }
            }
        }
        if (!classHasEqualsMethod) {
            report.addBug(new MissingEqualsMethodError(0, 0));
        }
    }

    public BugReport getReport() {
        return report;
    }
}
