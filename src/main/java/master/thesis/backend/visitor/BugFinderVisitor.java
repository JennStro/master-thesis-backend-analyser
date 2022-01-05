package master.thesis.backend.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import master.thesis.backend.errors.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BugFinderVisitor extends VoidVisitorAdapter<Void> {

    private BugReport report = new BugReport();
    private boolean shouldIgnoreNoEqualsMethodError = false;

    @Override
    public void visit(MethodCallExpr expression, Void arg) {
        super.visit(expression, arg);
        System.out.println(expression);
        System.out.println(expression.resolve().getReturnType());
        if (!expression.resolve().getReturnType().isVoid()) {
            if (expression.getParentNode().isPresent()) {
                if (expression.getParentNode().get().getMetaModel().getTypeName().equals("ExpressionStmt")) {
                    report.addBug(new IgnoringReturnError(0, 0));
                }
            } else {
                report.addBug(new IgnoringReturnError(0, 0));
            }
        }
    }

    /**
     * Check that objects is not compared with the equals operator.
     *
     * Check that binary operator are not used on booleans.
     *
     * @param expression
     * @param arg
     */
    @Override
    public void visit(BinaryExpr expression, Void arg) {
        super.visit(expression, arg);
        if (expression.getOperator().equals(BinaryExpr.Operator.EQUALS) || expression.getOperator().equals(BinaryExpr.Operator.NOT_EQUALS)) {
            if(!(expression.getLeft().calculateResolvedType().isPrimitive() && expression.getRight().calculateResolvedType().isPrimitive())) {
                report.addBug(new EqualsOperatorError(0, 0));
            }
        }
        if (expression.getOperator().equals(BinaryExpr.Operator.BINARY_OR) || expression.getOperator().equals(BinaryExpr.Operator.BINARY_AND)) {
            if (expression.getLeft().calculateResolvedType().describe().equals("boolean") && expression.getRight().calculateResolvedType().describe().equals("boolean")) {
                report.addBug(new BitwiseOperatorError(0, 0));
            }
        }
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
                for (Statement constructorChild : constructor.getBody().getStatements()) {
                    if (getFieldAccessExpr(constructorChild).isPresent()) {
                        FieldAccessExpr fieldAccessExpr = getFieldAccessExpr(constructorChild).get();
                        for (VariableDeclarator uninitializedFieldDeclaration : uninitializedFieldDeclarations) {
                            if (fieldAccessExpr.getNameAsString().equals(uninitializedFieldDeclaration.getNameAsString())) {
                                initializedFieldDeclarations.add(uninitializedFieldDeclaration);
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

    private Optional<FieldAccessExpr> getFieldAccessExpr(Statement constructorStatement) {
        if (constructorStatement.isExpressionStmt()) {
            for (Node expression : constructorStatement.getChildNodes()) {
                if (expression instanceof AssignExpr) {
                    for (Node fieldAccessExpressionCandidate : expression.getChildNodes()) {
                        if (fieldAccessExpressionCandidate instanceof FieldAccessExpr) {
                            FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) fieldAccessExpressionCandidate;
                            return Optional.of(fieldAccessExpr);
                        } else {
                            return Optional.empty();
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Check if if-statement has brackets or a semicolon after statement.
     * @param statement
     * @param arg
     */
    public void visit(IfStmt statement, Void arg) {
        super.visit(statement, arg);
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
