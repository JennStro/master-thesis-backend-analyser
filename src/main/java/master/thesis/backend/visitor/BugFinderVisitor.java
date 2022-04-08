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
import master.thesis.backend.annotations.AnnotationNames;
import master.thesis.backend.errors.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class BugFinderVisitor extends VoidVisitorAdapter<Void> {

    private BugReport report = new BugReport();
    private HashSet<String> errorsToIgnore = new HashSet<>();

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
        if (!errorsToIgnore.contains(AnnotationNames.EQUALS_OPERATOR_ERROR) && operator.equals(BinaryExpr.Operator.EQUALS) || operator.equals(BinaryExpr.Operator.NOT_EQUALS)) {
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
                    if (!errorsToIgnore.contains(AnnotationNames.EQUALS_OPERATOR_ERROR) && leftIsObjectReference && rightIsObjectReference) {
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
        if (!errorsToIgnore.contains(AnnotationNames.INTEGER_DIVISION_ERROR) && operator.equals(BinaryExpr.Operator.DIVIDE)) {
            if (!isInsidePrintStatement(expression)) {
                try {
                    if (left.calculateResolvedType().describe().equals("int") && right.calculateResolvedType().describe().equals("int")) {
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
                } catch (UnsolvedSymbolException unsolvedSymbolException) {
                    report.attach(unsolvedSymbolException);
                }
            }
        }

        if (!errorsToIgnore.contains(AnnotationNames.BITWISE_OPERATOR_ERROR) && operator.equals(BinaryExpr.Operator.BINARY_OR) || operator.equals(BinaryExpr.Operator.BINARY_AND)) {
            if (!isInsidePrintStatement(expression)) {
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
        List<Node> children = declaration.getChildNodes();

        boolean classHasEqualsMethod = false;
        boolean shouldIgnoreNoEqualsMethodError = false;

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
        }
        if (!classHasEqualsMethod && !shouldIgnoreNoEqualsMethodError && !declaration.isInterface() && !declaration.isAbstract()) {
            MissingEqualsMethodError error = new MissingEqualsMethodError();
            error.setContainingClass(declaration.getNameAsString());
            report.addBug(error);
        }

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
        if (thenStatementIsEmpty && !errorsToIgnore.contains(AnnotationNames.SEMICOLON_AFTER_IF_ERROR)) {
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
        else if (!thenStatementHasCurlyBraces && !errorsToIgnore.contains(AnnotationNames.IF_NO_BRACKETS_ERROR)) {
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
        ArrayList<BaseError> bugs = report.getBugs();
        ArrayList<BaseError> whiteListedBugs = new ArrayList<>();
        if (bugs.isEmpty()) {return report;}
        for (BaseError bug : bugs) {
            if (!errorsToIgnore.contains(bug.annotationName())) {
                whiteListedBugs.add(bug);
            }
        }
        report.setBugs(whiteListedBugs);
        return report;
    }

    @Override
    public void visit(MarkerAnnotationExpr annotationExpr, Void arg) {
        super.visit(annotationExpr, arg);
        if (!annotationExpr.toString().equals("@NoEqualsMethod")) {
            errorsToIgnore.add(annotationExpr.toString());
        }
    }
}
