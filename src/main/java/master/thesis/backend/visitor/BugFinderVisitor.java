package master.thesis.backend.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import master.thesis.backend.errors.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class BugFinderVisitor extends VoidVisitorAdapter<Void> {

    private BugReport report = new BugReport();

    private HashMap<String, Integer> env = new HashMap<>();

    /**
     * Save all declarations of integer in env
     * @param decl
     * @param arg
     */
    @Override
    public void visit(VariableDeclarator decl, Void arg) {
        super.visit(decl, arg);
        if (decl.getInitializer().isPresent() && decl.getInitializer().get().isIntegerLiteralExpr()) {
            env.put(decl.getName().asString(), Integer.parseInt(decl.getInitializer().get().toString()));
        }
    }

    /**
     * Save all updates or assignments of integer variables to env.
     * @param assignExpr
     * @param arg
     */
    @Override
    public void visit(AssignExpr assignExpr, Void arg) {
        super.visit(assignExpr, arg);
        String variableName = assignExpr.getTarget().toString();
        if (env.containsKey(variableName)) {
            env.put(variableName,  Integer.parseInt(assignExpr.getValue().toString()));
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
            if (!isInsideEqualsMethod(expression) && !isInsidePrintStatement(expression)) {
                try {
                    if (!isPrimitiveOrNull(left) && !isPrimitiveOrNull(right) && !ifMethodCallExpressionThenCheckIfItReturnsPrimitiveOrNull(left) && !ifMethodCallExpressionThenCheckIfItReturnsPrimitiveOrNull(right)) {
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
                        if (left.calculateResolvedType().isArray() && right.calculateResolvedType().isArray()) {
                            error.setArraysSuggestion();
                        }
                        report.addBug(error);
                    }
                } catch (UnsolvedSymbolException unsolvedSymbolException) {
                    // When a type is not resolved, we know it is not a primitive. But an object may be called upon, which can
                    // result in a primitive. So we need to check that it is only the object. If it is called upon, we add the
                    // exception. If not, we add the bug.
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
                    else {
                        report.attach(unsolvedSymbolException);
                    }
                }
            }


        }
        if (operator.equals(BinaryExpr.Operator.DIVIDE)) {
            if (!isInsidePrintStatement(expression)) {
                try {
                    if (left.calculateResolvedType().describe().equals("int") && right.calculateResolvedType().describe().equals("int")) {
                        if (!divisionResultsInInteger(left, right)) {
                            IntegerDivisionError integerDivisionError = new IntegerDivisionError();
                            if (getContainingClass(expression).isPresent()) {
                                integerDivisionError.setContainingClass(getContainingClass(expression).get());
                            }
                            int lineNumber = -1;
                            if (expression.getRange().isPresent()) {
                                lineNumber = expression.getRange().get().begin.line;
                            }
                            integerDivisionError.setLineNumber(lineNumber);
                            integerDivisionError.setLeftInteger(left.toString());
                            integerDivisionError.setRightInteger(right.toString());
                            report.addBug(integerDivisionError);
                        }
                    }
                } catch (UnsolvedSymbolException unsolvedSymbolException) {
                    report.attach(unsolvedSymbolException);
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
            } catch (UnsolvedSymbolException unsolvedSymbolException) {
                report.attach(unsolvedSymbolException);
            }
        }
    }

    private boolean divisionResultsInInteger(Expression left, Expression right) {
        return env.get(left.toString()) % env.get(right.toString()) == 0;
    }

    /**
     *
     * @param expr
     * @return True if is i a methodcall and it returns either primitive or null. False otherwise.
     * @throws UnsolvedSymbolException
     */
    private boolean ifMethodCallExpressionThenCheckIfItReturnsPrimitiveOrNull(Expression expr) throws UnsolvedSymbolException {
        if (expr.isMethodCallExpr()) {
            MethodCallExpr methodCallExpr = (MethodCallExpr) expr;
            ResolvedType returnType = methodCallExpr.resolve().getReturnType();
            return returnType.isPrimitive() || returnType.isNull();
        }
        return false;
    }

    private boolean isPrimitiveOrNull(Expression exp) throws UnsolvedSymbolException {
        return exp.calculateResolvedType().isPrimitive() || exp.calculateResolvedType().isNull();
    }

    private boolean isInsideEqualsMethod(Node node) {
        Optional<MethodDeclaration> methodParent = node.findAncestor(MethodDeclaration.class);
        return methodParent.isPresent() && methodParent.get().getNameAsString().equals("equals");
    }

    private boolean isInsidePrintStatement(Node node) {
        Optional<MethodCallExpr> methodCallExprOptionalParent = node.findAncestor(MethodCallExpr.class);
        return methodCallExprOptionalParent.isPresent() && (methodCallExprOptionalParent.get().getNameAsString().equals("println") || methodCallExprOptionalParent.get().getNameAsString().equals("print"));
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
        if (!classHasEqualsMethod && !shouldIgnoreNoEqualsMethodError && !declaration.isInterface()) {
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
            int lineNumberOfIfStatement = -1;
            int indentationThenStatement = -1;
            int lineNumberOfThenStatement = -1;

            if (statement.getRange().isPresent()) {
                lineNumberOfIfStatement = statement.getRange().get().begin.line;
            }
            if (thenStatement.getRange().isPresent()) {
                lineNumberOfThenStatement =  thenStatement.getRange().get().begin.line;
                indentationThenStatement = thenStatement.getRange().get().begin.column;
            }

            if (siblingOf(statement).isPresent()) {
                Node sibling = siblingOf(statement).get();
                int intendationSiblingStatement = sibling.getRange().get().begin.column;
                int lineNumberOfSiblingStatement = sibling.getRange().get().begin.line;

                if (indentationThenStatement == intendationSiblingStatement || lineNumberOfSiblingStatement == lineNumberOfIfStatement) {
                    IfWithoutBracketsError error = new IfWithoutBracketsError();
                    if (getContainingClass(statement).isPresent()) {
                        error.setContainingClass(getContainingClass(statement).get());
                    }
                    error.setLineNumber(lineNumberOfIfStatement);
                    error.setCondition(statement.getCondition().toString());
                    error.setThenBranch(statement.getThenStmt().toString());
                    report.addBug(error);
                }
            }
        }
    }

    private Optional<Node> siblingOf(IfStmt statement) {
        if (statement.hasParentNode()) {
            Node parent = statement.getParentNode().get();
            List<Node> children = parent.getChildNodes();
            int indexOfIfStatement = children.indexOf(statement);
            if (indexOfIfStatement+1 < children.size()) {
               return Optional.of(children.get(indexOfIfStatement+1));
            }
        }
        return Optional.empty();
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
