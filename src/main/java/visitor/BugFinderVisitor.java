package visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import errors.BugReport;
import errors.IfWithoutBracketsError;
import errors.MissingEqualsMethodError;

import java.util.List;

public class BugFinderVisitor extends VoidVisitorAdapter<Void> {

    private BugReport report = new BugReport();
    private boolean shouldIgnoreNoEqualsMethodError = false;

    @Override
    public void visit(MethodDeclaration declaration, Void arg) {
         super.visit(declaration, arg);

    }

    /**
     * Go through the classes children and look for the equals method.
     * If not found and @NoEqualsMethod is not used on class, add error.
     *
     * @param declaration
     * @param arg
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        super.visit(declaration, arg);
        report.setFileName(declaration.getNameAsString());
        List<Node> children = declaration.getChildNodes();
        boolean classHasEqualsMethod = false;
        for (Node child : children) {
            if (child instanceof MethodDeclaration) {
                MethodDeclaration equalsMethodCandidate = (MethodDeclaration) child;
                if (equalsMethodCandidate.getNameAsString().equals("equals")) {
                    classHasEqualsMethod = true;
                }
            }
        }
        if (!classHasEqualsMethod && !shouldIgnoreNoEqualsMethodError) {
            report.addBug(new MissingEqualsMethodError(0, 0));
        }
    }

    public void visit(IfStmt statement, Void arg) {
        if (!(statement.getThenStmt() instanceof BlockStmt)) {
            report.addBug(new IfWithoutBracketsError(0,0));
        }
    }

    public void visit(MarkerAnnotationExpr declaration, Void arg) {
        super.visit(declaration, arg);
        if (declaration.toString().equals("@NoEqualsMethod")) {
            this.shouldIgnoreNoEqualsMethodError = true;
        }
    }

    public BugReport getReport() {
        return report;
    }
}
