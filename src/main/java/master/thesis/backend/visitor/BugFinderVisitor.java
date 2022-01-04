package master.thesis.backend.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import master.thesis.backend.errors.*;

import java.util.ArrayList;
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
     * Check if fieldvariables have been initialized.
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

        ArrayList<VariableDeclarator> uninitializedFieldDeclarations = new ArrayList<>();
        ArrayList<VariableDeclarator> initializedFieldDeclarations = new ArrayList<>();

        for (Node child : children) {
            if (child instanceof MethodDeclaration) {
                MethodDeclaration equalsMethodCandidate = (MethodDeclaration) child;
                if (equalsMethodCandidate.getNameAsString().equals("equals")) {
                    classHasEqualsMethod = true;
                }
            }
            if (child instanceof FieldDeclaration) {
                FieldDeclaration field = (FieldDeclaration) child;
                for (VariableDeclarator varDecl : field.getVariables()) {
                    if (varDecl.getInitializer().isEmpty()) {
                        uninitializedFieldDeclarations.add(varDecl);
                    }
                }
            }
            if (child instanceof ConstructorDeclaration) {
                ConstructorDeclaration constructor = (ConstructorDeclaration) child;
                System.out.println(constructor.getBody());

                for (Statement constructorChild : constructor.getBody().getStatements()) {
                    if (constructorChild.isExpressionStmt()) {
                        for (Node expression : constructorChild.getChildNodes()) {
                            if (expression instanceof AssignExpr) {
                                for (Node fieldAccessExpressionCandidate : expression.getChildNodes()) {
                                    if (fieldAccessExpressionCandidate instanceof FieldAccessExpr) {
                                        FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) fieldAccessExpressionCandidate;
                                        for (VariableDeclarator uninitializedFieldDeclaration : uninitializedFieldDeclarations) {
                                            if (fieldAccessExpr.getNameAsString().equals(uninitializedFieldDeclaration.getNameAsString())) {
                                                initializedFieldDeclarations.add(uninitializedFieldDeclaration);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!classHasEqualsMethod && !shouldIgnoreNoEqualsMethodError) {
            report.addBug(new MissingEqualsMethodError(0, 0));
        }
        uninitializedFieldDeclarations.removeAll(initializedFieldDeclarations);
        if (!uninitializedFieldDeclarations.isEmpty()) {
            report.addBug(new FieldDeclarationWithoutInitializerError(0,0));
        }
    }

    /**
     * Check if if-statement has brackets.
     * @param statement
     * @param arg
     */
    public void visit(IfStmt statement, Void arg) {
        if (statement.getThenStmt().getMetaModel().getTypeName().equals("EmptyStmt")) {
            report.addBug(new SemiColonAfterIfError(0,0));
        }
        if (!(statement.getThenStmt() instanceof BlockStmt)) {
            report.addBug(new IfWithoutBracketsError(0,0));
        }
    }


    /**
     * Check if annotations for ignoring have been set.
     * @param declaration
     * @param arg
     */
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
