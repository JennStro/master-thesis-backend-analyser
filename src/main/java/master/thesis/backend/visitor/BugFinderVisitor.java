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

    /**
     * Fin methodcalls that have ignored return value.
     *
     * @param expression
     * @param arg
     */
    @Override
    public void visit(MethodCallExpr expression, Void arg) {
        super.visit(expression, arg);
        if (!expression.resolve().getReturnType().isVoid()) {
            if (expression.getParentNode().isPresent()) {
                boolean methodCallIsNotUsed = expression.getParentNode().get().getMetaModel().getTypeName().equals("ExpressionStmt");
                if (methodCallIsNotUsed) {
                    IgnoringReturnError error = new IgnoringReturnError(0, 0);
                    error.setReturnType(expression.resolve().getReturnType().describe());
                    error.setMethodCall(expression.toString());
                    report.addBug(error);
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
        BinaryExpr.Operator operator = expression.getOperator();
        Expression left = expression.getLeft();
        Expression right = expression.getRight();
        if (operator.equals(BinaryExpr.Operator.EQUALS) || operator.equals(BinaryExpr.Operator.NOT_EQUALS)) {
            boolean expressionsAreNotPrimitive = !(left.calculateResolvedType().isPrimitive() && right.calculateResolvedType().isPrimitive());
            if (expressionsAreNotPrimitive) {
                EqualsOperatorError error = new EqualsOperatorError(0, 0);
                error.setObjectOne(left.toString());
                error.setObjectTwo(right.toString());
                if (operator.equals(BinaryExpr.Operator.NOT_EQUALS)) {
                    error.withNegatedOperator();
                }
                report.addBug(error);
            }
        }
        if (operator.equals(BinaryExpr.Operator.BINARY_OR) || operator.equals(BinaryExpr.Operator.BINARY_AND)) {
            if (left.calculateResolvedType().describe().equals("boolean") && right.calculateResolvedType().describe().equals("boolean")) {
                BitwiseOperatorError error = new BitwiseOperatorError(0,0);
                error.setLeftOperand(left.toString());
                error.setRightOperand(right.toString());
                error.setOperator(operator.asString());
                report.addBug(error);
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
        for (VariableDeclarator uninitializedFieldDeclaration : uninitializedFieldDeclarations) {
            FieldDeclarationWithoutInitializerError error = new FieldDeclarationWithoutInitializerError(0,0);
            error.setClass(declaration.getNameAsString());
            error.setFieldVariableName(uninitializedFieldDeclaration.getNameAsString());
            error.setFieldVariableType(uninitializedFieldDeclaration.getType().asString());
            report.addBug(error);
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
        Statement thenStatement = statement.getThenStmt();
        boolean thenStatementIsEmpty = thenStatement.getMetaModel().getTypeName().equals("EmptyStmt");
        boolean thenStatementHasCurlyBraces = thenStatement instanceof BlockStmt;
        if (thenStatementIsEmpty) {
            SemiColonAfterIfError error = new SemiColonAfterIfError(0,0);
            error.setCondition(statement.getCondition().toString());
            report.addBug(error);
        }
        if (!thenStatementHasCurlyBraces) {
            IfWithoutBracketsError error = new IfWithoutBracketsError(0,0);
            error.setCondition(statement.getCondition().toString());
            error.setThenBranch(statement.getThenStmt().toString());
            report.addBug(error);
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
