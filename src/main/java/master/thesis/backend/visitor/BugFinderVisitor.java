package master.thesis.backend.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import master.thesis.backend.analyser.AnalyserConfiguration;
import master.thesis.backend.errors.*;
import java.util.List;
import java.util.Optional;

/**
 * A visitor for finding the following bugs:
 *
 *  A semicolon after if statement:
 *      if (something); {}
 *
 *  Using bitwise operators on boolean expressions:
 *      boolean a = true & false;
 *      boolean b = true | false;
 *
 *  Using equals operator on objects:
 *      Object o1 = new Object();
 *      Object o2 = new Object();
 *      boolean a = o1 == o2;
 *
 *  Expecting double from integer division:
 *      double a = 7/5;
 *
 *  Wrong indentation of if-statements:
 *      if (something)
 *          doSomething();
 *          doSomethingElse();
 *
 *  Not implementing the equals method in a class.
 *
 * Bugs can be ignored by implementing {@link AnalyserConfiguration}.
 */
public class BugFinderVisitor extends VoidVisitorAdapter<Void> {

    private BugReport report = new BugReport();
    AnalyserConfiguration configuration;

    /**
     *
     * @param configuration for how bugs should be ignored. Set to null if no configuration.
     */
    public BugFinderVisitor(AnalyserConfiguration configuration) { super(); this.configuration = configuration;}

    /**
     * Check that objects are not compared with the equals operator.
     * If found, add a {@link EqualsOperatorError} to {@link BugReport}
     * Ignored when expression is inside print statement and equals method declaration.
     *
     * Check that integer division does not expect a double.
     * If found, add a {@link IntegerDivisionError} to {@link BugReport}
     * Ignored when expression is inside print statement.
     *
     * Check that binary operator are not used on booleans.
     * If found, add a {@link BitwiseOperatorError} to {@link BugReport}
     * Ignored when expression is inside print statement.
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

        if (equalsOperatorIsUsedIn(expression)) {
            if (!isInsideEqualsMethod(expression) && !isInsidePrintStatement(expression)) {
                try {
                    if (equalsOperatorIsNotUsedToCompareNullOrPrimitivesIn(expression)) {
                        int lineNumber = getLineNumberFrom(expression);
                        EqualsOperatorError equalsOperatorError = new EqualsOperatorError();
                        if (getContainingClass(expression).isPresent()) {
                            equalsOperatorError.setContainingClass(getContainingClass(expression).get());
                        }
                        equalsOperatorError.setLineNumber(lineNumber);
                        equalsOperatorError.setLeftOperand(left.toString());
                        equalsOperatorError.setRightOperand(right.toString());
                        equalsOperatorError.setOperator(expression.getOperator().asString());
                        if (left.calculateResolvedType().isArray() && right.calculateResolvedType().isArray()) {
                            equalsOperatorError.setArraysSuggestion();
                        }
                        if (shouldBeAddedToReport(equalsOperatorError)) {
                            report.addBug(equalsOperatorError);
                        }
                    }
                } catch (UnsolvedSymbolException unsolvedSymbolException) {
                    // When a type is not resolved, we know it is not a primitive. But an object may be called upon, which can
                    // result in a primitive. So we need to check that it is only the object. If it is called upon, we add the
                    // exception. If not, we add the bug.
                    boolean leftIsObjectReference = left.isNameExpr();
                    boolean rightIsObjectReference = right.isNameExpr();
                    if (leftIsObjectReference && rightIsObjectReference) {
                        int lineNumber = getLineNumberFrom(expression);
                        EqualsOperatorError equalsOperatorError = new EqualsOperatorError();
                        if (getContainingClass(expression).isPresent()) {
                            equalsOperatorError.setContainingClass(getContainingClass(expression).get());
                        }
                        equalsOperatorError.setLineNumber(lineNumber);
                        equalsOperatorError.setLeftOperand(left.toString());
                        equalsOperatorError.setRightOperand(right.toString());
                        equalsOperatorError.setOperator(expression.getOperator().asString());

                            report.addBug(equalsOperatorError);

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
                        if (!isInVariableDeclarationDefinedAsInteger(expression) && !isInFieldDeclarationDefinedAsInteger(expression) && !isReturningIntegerInMethodExpectingInteger(expression)) {

                            IntegerDivisionError integerDivisionError = new IntegerDivisionError();
                            if (getContainingClass(expression).isPresent()) {
                                integerDivisionError.setContainingClass(getContainingClass(expression).get());
                            }
                            int lineNumber = getLineNumberFrom(expression);
                            integerDivisionError.setLineNumber(lineNumber);
                            integerDivisionError.setLeftOperand(left.toString());
                            integerDivisionError.setRightOperand(right.toString());
                            if (shouldBeAddedToReport(integerDivisionError)) {
                                report.addBug(integerDivisionError);
                            }
                        }
                    }
                } catch (UnsolvedSymbolException unsolvedSymbolException) {
                    report.attach(unsolvedSymbolException);
                }
            }
        }

        if (operator.equals(BinaryExpr.Operator.BINARY_OR) || operator.equals(BinaryExpr.Operator.BINARY_AND)) {
            if (!isInsidePrintStatement(expression)) {
                try {
                    if (left.calculateResolvedType().describe().equals("boolean") && right.calculateResolvedType().describe().equals("boolean")) {
                        int lineNumber = getLineNumberFrom(expression);
                        BitwiseOperatorError bitwiseOperatorError = new BitwiseOperatorError();
                        if (getContainingClass(expression).isPresent()) {
                            bitwiseOperatorError.setContainingClass(getContainingClass(expression).get());
                        }
                        bitwiseOperatorError.setLineNumber(lineNumber);
                        bitwiseOperatorError.setLeftOperand(left.toString());
                        bitwiseOperatorError.setRightOperand(right.toString());
                        bitwiseOperatorError.setOperator(operator.asString());
                        if (shouldBeAddedToReport(bitwiseOperatorError)) {
                            report.addBug(bitwiseOperatorError);
                        }
                    }
                } catch (UnsolvedSymbolException unsolvedSymbolException) {
                    report.attach(unsolvedSymbolException);
                }
            }
        }
    }

    /**
     * Used to check the expected type of integer division and limit false positives of {@link IntegerDivisionError}
     *
     * Go up the tree until a variabledeclaration is found. Check if the variable is declared as int.
     *
     * @param expression
     * @return true if type is declared as int
     */
    private boolean isInVariableDeclarationDefinedAsInteger(BinaryExpr expression) {
        Optional<VariableDeclarationExpr> maybeVariableDeclarationExpr = expression.findAncestor(VariableDeclarationExpr.class);
        return maybeVariableDeclarationExpr.map(variableDeclarationExpr -> variableDeclarationExpr.calculateResolvedType().describe().equals("int")).orElse(false);
    }

    /**
     * Used to check the expected type of integer division and limit false positives of {@link IntegerDivisionError}
     *
     * Go up the tree until a fielddeclaration is found. Check if the field is declared as int.
     *
     * @param expression
     * @return true if type is declared as int
     */
    private boolean isInFieldDeclarationDefinedAsInteger(BinaryExpr expression) {
        Optional<FieldDeclaration> maybeFieldDeclarationExpr = expression.findAncestor(FieldDeclaration.class);
        return maybeFieldDeclarationExpr.map(fieldDeclaration -> fieldDeclaration.getVariables().get(0).resolve().getType().describe().equals("int")).orElse(false);
    }

    /**
     * Used to check the expected type of integer division and limit false positives of {@link IntegerDivisionError}
     *
     * Go up the tree until a returnstatement is found. Then, check if the returnstatement has expected type integer.
     *
     * @param expression
     * @return true if type is declared as int
     */
    private boolean isReturningIntegerInMethodExpectingInteger(BinaryExpr expression) {
        Optional<ReturnStmt> maybeReturnStmt = expression.findAncestor(ReturnStmt.class);
        if (maybeReturnStmt.isPresent()) {
            ReturnStmt returnStmt = maybeReturnStmt.get();
            Optional<MethodDeclaration> maybeMethodDeclaration = returnStmt.findAncestor(MethodDeclaration.class);
            if (maybeMethodDeclaration.isPresent()) {
                MethodDeclaration methodDeclaration = maybeMethodDeclaration.get();
                return methodDeclaration.getType().asString().equals("int");
            }
        }
        return false;
    }

    /**
     * Check if equals operator is used to find {@link EqualsOperatorError}
     *
     * @param expression
     * @return true if equals operator is used
     */
    private boolean equalsOperatorIsUsedIn(BinaryExpr expression) {
        BinaryExpr.Operator operator = expression.getOperator();
        return operator.equals(BinaryExpr.Operator.EQUALS) || operator.equals(BinaryExpr.Operator.NOT_EQUALS);
    }

    /**
     * Used to limit false positives of {@link EqualsOperatorError}
     * Check if operands are null or primitives.
     *
     * @param expression
     * @return true if operands are null or primitives
     */
    private boolean equalsOperatorIsNotUsedToCompareNullOrPrimitivesIn(BinaryExpr expression) {
        Expression left = expression.getLeft();
        Expression right = expression.getRight();
        return !isPrimitiveOrNull(left) && !isPrimitiveOrNull(right) && !ifMethodCallExpressionThenCheckIfItReturnsPrimitiveOrNull(left) && !ifMethodCallExpressionThenCheckIfItReturnsPrimitiveOrNull(right);
    }

    /**
     * Uses {@link AnalyserConfiguration} to check for errors to ignore.
     *
     * Check if {@link BaseError#getName()} is present in {@link AnalyserConfiguration} list of errors to ignore
     * for class {@link BaseError#getContainingClass()}.
     *
     * @param error the error to be checked
     * @return false if configuration is null. true is configuration finds error to be ignored for the containgclass.
     */
    private boolean shouldBeAddedToReport(BaseError error) {
        if (configuration == null) {
            return true;
        }
        return !configuration.getErrorsToIgnoreForClass(error.getContainingClass()).contains(error.getName());
    }

    /**
     * Find the linenumber for the errors.
     * @param expression
     * @return -1 if not found, else the line number of the expression
     */
    private int getLineNumberFrom(BinaryExpr expression) {
        int lineNumber = -1;
        if (expression.getRange().isPresent()) {
            lineNumber = expression.getRange().get().begin.line;
        }
        return lineNumber;
    }

    /**
     *  Used to limit false positives of {@link EqualsOperatorError}.
     *
     * Used by {@link #equalsOperatorIsNotUsedToCompareNullOrPrimitivesIn(BinaryExpr)} to check if
     * the equals operator is used on null or primitives.
     *
     * @param expr
     * @return True if is a methodcall and it returns either primitive or null. False otherwise.
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

    /**
     * Used to limit false positives of {@link EqualsOperatorError}.
     *
     * Used by {@link #equalsOperatorIsNotUsedToCompareNullOrPrimitivesIn(BinaryExpr)} to check if
     * the equals operator is used on null or primitives.
     *
     * @param exp
     * @return True if exp is either primitive or null. False otherwise.
     * @throws UnsolvedSymbolException
     */
    private boolean isPrimitiveOrNull(Expression exp) throws UnsolvedSymbolException {
        return exp.calculateResolvedType().isPrimitive() || exp.calculateResolvedType().isNull();
    }

    /**
     * Used to limit false positives of {@link EqualsOperatorError}.
     *
     * Go up the tree until a mtehod declaration is found, check if it is equals method.
     *
     * @param node
     * @return true if node is inside equals method declaration
     */
    private boolean isInsideEqualsMethod(Node node) {
        Optional<MethodDeclaration> methodParent = node.findAncestor(MethodDeclaration.class);
        return methodParent.isPresent() && methodParent.get().getNameAsString().equals("equals");
    }

    /**
     * Used to limit false positives of {@link EqualsOperatorError}, {@link IntegerDivisionError}, {@link BitwiseOperatorError}.
     *
     * Go up the tree until a print statement is found.
     *
     * @param node
     * @return true if node is inside print statement
     */
    private boolean isInsidePrintStatement(Node node) {
        Optional<MethodCallExpr> methodCallExprOptionalParent = node.findAncestor(MethodCallExpr.class);
        return methodCallExprOptionalParent.isPresent() && (methodCallExprOptionalParent.get().getNameAsString().equals("println") || methodCallExprOptionalParent.get().getNameAsString().equals("print"));
    }

    /**
     * Go through method declarations to find an equals method.
     * If not found, add a {@link MissingEqualsMethodError} to {@link BugReport}
     * Ignore if interface or abstract class.
     *
     * @param declaration
     * @param arg
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        super.visit(declaration, arg);
        List<Node> children = declaration.getChildNodes();

        report.setClassName(declaration.getNameAsString());

        boolean classHasEqualsMethod = false;

        for (Node child : children) {
            if (child instanceof MethodDeclaration) {
                MethodDeclaration equalsMethodCandidate = (MethodDeclaration) child;
                if (equalsMethodCandidate.getNameAsString().equals("equals")) {
                    classHasEqualsMethod = true;
                }
            }
        }
        if (!classHasEqualsMethod && !declaration.isInterface() && !declaration.isAbstract()) {
            MissingEqualsMethodError missingEqualsMethodError = new MissingEqualsMethodError();
            missingEqualsMethodError.setContainingClass(declaration.getNameAsString());
            if (shouldBeAddedToReport(missingEqualsMethodError)) {
                report.addBug(missingEqualsMethodError);
            }
        }

    }

    /**
     * Check if if-statement has brackets. If not, check if the statement has a sibling, and check if
     * the sibling is indented wrong. If so, add a {@link IfWithoutBracketsError} to {@link BugReport}.
     *
     * Check if the if-statment has empty statement as body. If so, add {@link SemiColonAfterIfError} to {@link BugReport}.
     *
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
            SemiColonAfterIfError semiColonAfterIfError = new SemiColonAfterIfError();
            if (getContainingClass(statement).isPresent()) {
                semiColonAfterIfError.setContainingClass(getContainingClass(statement).get());
            }
            semiColonAfterIfError.setLineNumber(lineNumber);
            semiColonAfterIfError.setCondition(statement.getCondition().toString());
            if (shouldBeAddedToReport(semiColonAfterIfError)) {
                report.addBug(semiColonAfterIfError);
            }

        }
        else if (!thenStatementHasCurlyBraces) {
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

                if (indentationThenStatement == intendationSiblingStatement || lineNumberOfSiblingStatement == lineNumberOfIfStatement || lineNumberOfSiblingStatement == lineNumberOfThenStatement) {
                    IfWithoutBracketsError ifWithoutBracketsError = new IfWithoutBracketsError();
                    if (getContainingClass(statement).isPresent()) {
                        ifWithoutBracketsError.setContainingClass(getContainingClass(statement).get());
                    }
                    ifWithoutBracketsError.setLineNumber(lineNumberOfIfStatement);
                    ifWithoutBracketsError.setCondition(statement.getCondition().toString());
                    ifWithoutBracketsError.setThenBranch(statement.getThenStmt().toString());
                    if (shouldBeAddedToReport(ifWithoutBracketsError)) {
                        report.addBug(ifWithoutBracketsError);
                    }
                }
            }
        }
    }

    /**
     * Find the sibling of a node. Used by {@link #visit(IfStmt, Void )} to find {@link IfWithoutBracketsError}.
     *
     * @param statement
     * @return empty if not found, the sibling if found
     */
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

    /**
     * Get the containing class of a node. Used to set {@link BaseError#getContainingClass()}.
     * @param node
     * @return empty if not found, string of containing class if found
     */
    private Optional<String> getContainingClass(Node node) {
        Optional<ClassOrInterfaceDeclaration> maybeContainingClass = node.findAncestor(ClassOrInterfaceDeclaration.class);
        if (maybeContainingClass.isPresent()) {
            ClassOrInterfaceDeclaration clazz = maybeContainingClass.get();
            return Optional.of(clazz.getNameAsString());
        }
        return Optional.empty();
    }

    /**
     *
     * @return the bugreport for this analysis.
     */
    public BugReport getReport() {
        return this.report;
    }
}
