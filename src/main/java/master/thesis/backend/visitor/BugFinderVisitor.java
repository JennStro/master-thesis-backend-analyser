package master.thesis.backend.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import master.thesis.backend.errors.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BugFinderVisitor extends VoidVisitorAdapter<Void> {

    private BugReport report = new BugReport();

    /**
     * Find methodcalls that have ignored return value.
     *
     * @param expression
     * @param arg
     */
    @Override
    public void visit(MethodCallExpr expression, Void arg) {
        super.visit(expression, arg);
        try {
            expression.resolve();
            if (!expression.resolve().getReturnType().isVoid() && !expression.resolve().getReturnType().describe().equals("boolean")) {
                if (expression.getParentNode().isPresent()) {
                    boolean methodCallIsNotUsed = expression.getParentNode().get().getMetaModel().getTypeName().equals("ExpressionStmt");
                    if (methodCallIsNotUsed) {
                        int lineNumber = -1;
                        if (expression.getRange().isPresent()) {
                            lineNumber = expression.getRange().get().begin.line;
                        }
                        IgnoringReturnError error = new IgnoringReturnError();
                        if (getContainingClass(expression).isPresent()) {
                            error.setContainingClass(getContainingClass(expression).get());
                        }
                        error.setLineNumber(lineNumber);
                        error.setReturnType(expression.resolve().getReturnType().describe());
                        error.setMethodCall(expression.toString());
                        report.addBug(error);
                    }
                } else {
                    report.addBug(new IgnoringReturnError());
                }
            }
        } catch (UnsolvedSymbolException ignore) {
            //When a methodcall is unresolved, we can not find the returntype, so we can not check if it returns void.
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

            try {
                boolean expressionsArePrimitiveOrNull =
                        left.calculateResolvedType().isPrimitive()
                        || right.calculateResolvedType().isPrimitive()
                        || left.calculateResolvedType().isNull()
                        || right.calculateResolvedType().isNull();
                if (!expressionsArePrimitiveOrNull) {
                    int lineNumber = -1;
                    if (expression.getRange().isPresent()) {
                        lineNumber = expression.getRange().get().begin.line;
                    }
                    EqualsOperatorError error = new EqualsOperatorError();
                    if (getContainingClass(expression).isPresent()) {
                        error.setContainingClass(getContainingClass(expression).get());
                    }
                    error.setLineNumber(lineNumber);
                    error.setObjectOne(left.toString());
                    error.setObjectTwo(right.toString());
                    if (operator.equals(BinaryExpr.Operator.NOT_EQUALS)) {
                        error.withNegatedOperator();
                    }
                    report.addBug(error);
                }
            } catch (UnsolvedSymbolException unsolvedSymbolException) {
                // When a type is not resolved, it in not a primitive. But an object may be called upon, which can
                // result in a primitive. So we need to check that it is only the object.
                boolean leftIsObjectReference = left.isNameExpr();
                boolean rightIsObjectReference = right.isNameExpr();
                if (leftIsObjectReference && rightIsObjectReference) {
                    int lineNumber = -1;
                    if (expression.getRange().isPresent()) {
                        lineNumber = expression.getRange().get().begin.line;
                    }
                    EqualsOperatorError error = new EqualsOperatorError();
                    if (getContainingClass(expression).isPresent()) {
                        error.setContainingClass(getContainingClass(expression).get());
                    }
                    error.setLineNumber(lineNumber);
                    error.setObjectOne(left.toString());
                    error.setObjectTwo(right.toString());
                    if (operator.equals(BinaryExpr.Operator.NOT_EQUALS)) {
                        error.withNegatedOperator();
                    }
                    report.addBug(error);
                }
            }


        }
        if (operator.equals(BinaryExpr.Operator.BINARY_OR) || operator.equals(BinaryExpr.Operator.BINARY_AND)) {
            try {
                if (left.calculateResolvedType().describe().equals("boolean") && right.calculateResolvedType().describe().equals("boolean")) {
                    int lineNumber = -1;
                    if (expression.getRange().isPresent()) {
                        lineNumber = expression.getRange().get().begin.line;
                    }
                    BitwiseOperatorError error = new BitwiseOperatorError();
                    if (getContainingClass(expression).isPresent()) {
                        error.setContainingClass(getContainingClass(expression).get());
                    }
                    error.setLineNumber(lineNumber);
                    error.setLeftOperand(left.toString());
                    error.setRightOperand(right.toString());
                    error.setOperator(operator.asString());
                    report.addBug(error);
                }
            } catch (UnsolvedSymbolException ignore) {}
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
        report.setClassName(declaration.getNameAsString());
        List<Node> children = declaration.getChildNodes();

        boolean classHasEqualsMethod = false;
        boolean shouldIgnoreNoEqualsMethodError = false;

        ArrayList<VariableDeclarator> uninitializedFieldDeclarations = new ArrayList<>();
        ArrayList<VariableDeclarator> initializedFieldDeclarations = new ArrayList<>();

        for (Node child : children) {

            if (child instanceof MarkerAnnotationExpr) {
                if (child.toString().equals("@NoEqualsMethod")) {
                    shouldIgnoreNoEqualsMethodError = true;
                }
            }
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
                        if (!field.toString().contains("@NoInitialization")) {
                            uninitializedFieldDeclarations.add(varDecl);
                        }
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
            MissingEqualsMethodError error = new MissingEqualsMethodError();
            error.setContainingClass(declaration.getNameAsString());
            report.addBug(error);
        }
        uninitializedFieldDeclarations.removeAll(initializedFieldDeclarations);
        for (VariableDeclarator uninitializedFieldDeclaration : uninitializedFieldDeclarations) {
            int lineNumber = -1;
            if (uninitializedFieldDeclaration.getRange().isPresent()) {
                lineNumber = uninitializedFieldDeclaration.getRange().get().begin.line;
            }
            FieldDeclarationWithoutInitializerError error = new FieldDeclarationWithoutInitializerError();
            error.setLineNumber(lineNumber);
            error.setContainingClass(declaration.getNameAsString());
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
            int lineNumber = -1;
            if (statement.getRange().isPresent()) {
                lineNumber = statement.getRange().get().begin.line;
            }
            SemiColonAfterIfError error = new SemiColonAfterIfError();
            if (getContainingClass(statement).isPresent()) {
                error.setContainingClass(getContainingClass(statement).get());
            }
            error.setLineNumber(lineNumber);
            error.setCondition(statement.getCondition().toString());
            report.addBug(error);
        }
        if (!thenStatementHasCurlyBraces) {
            int lineNumber = -1;
            if (statement.getRange().isPresent()) {
                lineNumber = statement.getRange().get().begin.line;
            }
            IfWithoutBracketsError error = new IfWithoutBracketsError();
            if (getContainingClass(statement).isPresent()) {
                error.setContainingClass(getContainingClass(statement).get());
            }
            error.setLineNumber(lineNumber);
            error.setCondition(statement.getCondition().toString());
            error.setThenBranch(statement.getThenStmt().toString());
            report.addBug(error);
        }
    }

    private Optional<String> getContainingClass(Node node) {
        Optional<ClassOrInterfaceDeclaration> maybeContainingClass = node.findAncestor(ClassOrInterfaceDeclaration.class);
        if (maybeContainingClass.isPresent()) {
            ClassOrInterfaceDeclaration clazz = maybeContainingClass.get();
            return Optional.of(clazz.getNameAsString());
        }
        return Optional.empty();
    }

    public BugReport getReport() {
        return report;
    }
}
