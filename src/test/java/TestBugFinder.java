import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import master.thesis.backend.adapter.AnnotationsAdapter;
import master.thesis.backend.analyser.Analyser;
import master.thesis.backend.errors.*;
import master.thesis.backend.visitor.AnnotationVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import master.thesis.backend.visitor.BugFinderVisitor;

import java.io.File;
import java.io.FileNotFoundException;

public class TestBugFinder {

    private BugFinderVisitor visitor = new BugFinderVisitor();

    @BeforeAll
    static void setUp() {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        StaticJavaParser.getConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));
    }

    @Test
    public void shouldGiveErrorWhenMissingEqualsMethod() {
        String code = "class A {}";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof MissingEqualsMethodError);
    }

    @Test
    public void shouldNotGiveErrorWhenMissingEqualsMethodWithAnnotation() {
        String code = "@NoEqualsMethod class A {}";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void shouldNotGiveErrorWhenMissingEqualsMethodWithAnnotationReadFromFile() {
        String path = "src/test/java/TestClass.java";
        CompilationUnit compilationUnit = null;
        try {
            compilationUnit = StaticJavaParser.parse(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        AnnotationVisitor annotationVisitor = new AnnotationVisitor();
        annotationVisitor.visit(compilationUnit, null);
        AnnotationsAdapter adapter = new AnnotationsAdapter();
        BugFinderVisitor visitor = new BugFinderVisitor(adapter.getErrorsToIgnoreAsName(annotationVisitor.getAnnotations()));
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void shouldNotGiveErrorWhenIfStatementWithBrackets() {
        String code = "@NoEqualsMethod class A { public void method() {if (true) {} }}";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void shouldAllowIfStatementWithOneStatementWithoutBrackets() {
        String code = "@NoEqualsMethod class A { public void method() {if (true) \n System.out.println(\"\"); }}";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void shouldGiveErrorWhenSemicolonAfterIfStatement() {
        String code = "@NoEqualsMethod class A { public void method() {if (true); {System.out.println(\"\");} }}";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof SemiColonAfterIfError);
    }

    @Test
    public void shouldGiveErrorWhenTwoStatementsInIfWithoutBrackets() {
        String code = "@NoEqualsMethod class A { public void method() {if (true) " +
                "System.out.println(\"\");" +
                "System.out.println(\"\");" +
                "}}";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof IfWithoutBracketsError);
    }

    @Test
    public void shouldGiveErrorWhenTwoObjectsAreComparedUsingEqualsOperator() {
        String code = "@NoEqualsMethod class A { public A(Object a, Object b) { boolean bo = a==b; } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof EqualsOperatorError);
    }

    @Test
    public void shouldGiveErrorWhenTwoObjectsAreComparedUsingNegatedEqualsOperator() {
        String code = "@NoEqualsMethod class A { public A(Object a, Object b) { boolean bo = a!=b; } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof EqualsOperatorError);
    }

    @Test
    public void shouldNotGiveErrorWhenEqualsOperatorIsUsedOnPrimitive() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { boolean bo = a==b; } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void shouldNotGiveErrorWhenEqualsOperatorIsUsedOnNull() {
        String code = "@NoEqualsMethod class A { public A(Object a) { if (a == null) {} } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void shouldSuggestToUseEqualsMethodOnObjectsWhenUsingEqualsOperator() {
        String code = "@NoEqualsMethod class A { public A(Object a, Object b) { boolean bo = a==b; } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("a.equals(b)", error.getSuggestion().get());
    }

    @Test
    public void shouldSuggestToUseNegatedEqualsMethodOnObjectsWhenUsingNegatedEqualsOperator() {
        String code = "@NoEqualsMethod class A { public A(Object a, Object b) { boolean bo = a!=b; } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("!a.equals(b)", error.getSuggestion().get());
    }

    @Test
    public void semiAfterIfSuggestion() {
        String code = "@NoEqualsMethod class A { public void method() {if (true); {System.out.println(\"\");} }}";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("to remove the semicolon after the if-condition: if (true) { // The rest of your code }", error.getSuggestion().get());
    }

    @Test
    public void ifWithoutBlockOneStatementAllowed() {
        String code = "@NoEqualsMethod class A { public void method() {if (true) \n System.out.println(\"\"); }}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void unresolvedMethodCallException() {
        String code = "@NoEqualsMethod class A { public String method() { Bar b = new Bar(); return b.toString(); } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    /**
     * When a methodcall is unresolved, we can not find the returntype, so we can not check
     * if it returns void.
     */
    @Test
    public void unresolvedMethodCallExceptionEqualsOperator() {
        String code = "@NoEqualsMethod class A { public boolean method() { Bar b = new Bar(); Bar b2 = new Bar(); return b.getInt() == b2.getInt(); } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getException().isPresent());
    }

    @Test
    public void resolvedMethodCallExceptionEqualsOperator() {
        String code = "@NoEqualsMethod class A { @NoEqualsMethod class Bar {public int getInt() {return 2;}} public boolean method() { Bar b = new Bar(); Bar b2 = new Bar(); return b.getInt() == b2.getInt(); } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
        Assertions.assertFalse(report.getException().isPresent());
    }

    @Test
    public void resolvedMethodCallException() {
        String code = "@NoEqualsMethod class A { @NoEqualsMethod class Bar {  } public String method() { Bar b = new Bar(); b.toString(); return b; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
        Assertions.assertFalse(report.getException().isPresent());
    }

    @Test
    public void unresolvedVariableExceptionEqualsOperator() {
        String code = "@NoEqualsMethod class A { public String method(Bar b, Bar b1) { if (b==b1) {return b.toString();} } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("b.equals(b1)", error.getSuggestion().get());
    }

    @Test
    public void unresolvedVariableExceptionEqualsOperatorNoError() {
        String code = "@NoEqualsMethod class A { public String method(Bar b, Bar b1) { if (b.getNumber() ==b1.getNumber()) {return b.toString();} } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getException().isPresent());
    }

    @Test
    public void shouldFindEqualsOperatorErrorOnLineSix() {
        String code = "@NoEqualsMethod class A { \n" +
                "   @NoEqualsMethod class Bar {  } \n" +
                "   public boolean method() { \n" +
                "       Bar b = new Bar(); \n" +
                "       Bar b2 = new Bar(); \n" +
                "        return b==b2; " +
                "   } " +
                "}";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals(6, error.getLineNumber());
    }

    @Test
    public void noEqualsMethodTestWithAnnotationTestClassWithInnerClass() {
        String path = "src/test/java/FirstTestClass.java";
        CompilationUnit compilationUnit = null;
        try {
            compilationUnit = StaticJavaParser.parse(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        AnnotationVisitor annotationVisitor = new AnnotationVisitor();
        annotationVisitor.visit(compilationUnit, null);
        AnnotationsAdapter adapter = new AnnotationsAdapter();
        BugFinderVisitor visitor = new BugFinderVisitor(adapter.getErrorsToIgnoreAsName(annotationVisitor.getAnnotations()));
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
    }

    @Test
    public void ignoreEqualsInnerClass() {
        String path = "src/test/java/IgnoreEqualsOnlyInnerClass.java";
        CompilationUnit compilationUnit = null;
        try {
            compilationUnit = StaticJavaParser.parse(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("IgnoreEqualsOnlyInnerClass", error.getContainingClass());
        Assertions.assertEquals("to add the method @Override public boolean equals(Object o) { // Checks to decide if two objects are equal goes here }", error.getSuggestion().get());
    }

    @Test
    public void useEqualsOperatorClass() {
        String path = "src/test/java/EqualsOperatorClass.java";
        CompilationUnit compilationUnit = null;
        try {
            compilationUnit = StaticJavaParser.parse(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        AnnotationVisitor annotationVisitor = new AnnotationVisitor();
        annotationVisitor.visit(compilationUnit, null);
        AnnotationsAdapter adapter = new AnnotationsAdapter();
        BugFinderVisitor visitor = new BugFinderVisitor(adapter.getErrorsToIgnoreAsName(annotationVisitor.getAnnotations()));
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("EqualsOperatorClass", error.getContainingClass());
        Assertions.assertEquals("i.equals(i2)", error.getSuggestion().get());
    }

    @Test
    public void ifWithoutBracketsClass() {
        String path = "src/test/java/IfWithoutBracketsClass.java";
        CompilationUnit compilationUnit = null;
        try {
            compilationUnit = StaticJavaParser.parse(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        AnnotationVisitor annotationVisitor = new AnnotationVisitor();
        annotationVisitor.visit(compilationUnit, null);
        AnnotationsAdapter adapter = new AnnotationsAdapter();
        BugFinderVisitor visitor = new BugFinderVisitor(adapter.getErrorsToIgnoreAsName(annotationVisitor.getAnnotations()));
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("IfWithoutBracketsClass", error.getContainingClass());
        Assertions.assertEquals("if (shouldAddToList) {list.add(\"1\");...}", error.getSuggestion().get());
    }

    @Test
    public void equalsOperatorOnObjectInEqualsMethodIsAllowed() {
        String code = "class A { public boolean equals(Object o) {return this == o;}}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void equalsMethodNotInInterface() {
        String code = "interface A { }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void syntaxErrorTest() {
        String code = "@NoEqualsMethod class A public void method(B b) {} }";
        Analyser analyser = new Analyser();
        BugReport report = analyser.analyse(code);
        Assertions.assertTrue(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getException().isPresent());
    }

    @Test
    public void integerDivisionTest() {
        String code = "@NoEqualsMethod class A { public void method() {int a = 7; int b = 5; if(a/b==1.4) {System.out.println(\"Success\");}} }";
        Analyser analyser = new Analyser();
        BugReport report = analyser.analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
    }

    @Test
    public void integerDivisionCastingTest() {
        String code = "@NoEqualsMethod class A { public void method() {int a = 7; int b = 5; if((double)a/(double)b==1.4) {System.out.println(\"Success\");}} }";
        Analyser analyser = new Analyser();
        BugReport report = analyser.analyse(code);
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void integerDivisionSuggestionTest() {
        String code = "@NoEqualsMethod class A { public void method() {int a = 7; int b = 5; if(a/b==1.4) {System.out.println(\"Success\");}} }";
        Analyser analyser = new Analyser();
        BugReport report = analyser.analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("(double)a/(double)b", error.getSuggestion().get());
        Assertions.assertEquals("A", error.getContainingClass());
    }

    @Test
    public void notAddErrorIfInPrintStatement() {
        String codeIntegerDivision = "@NoEqualsMethod class A { public void method() {int a = 7; int b = 5; System.out.println(a/b);} }";
        Analyser analyser = new Analyser();
        BugReport reportIntegerDivision  = analyser.analyse(codeIntegerDivision);
        Assertions.assertTrue(reportIntegerDivision.getBugs().isEmpty());

        String codeEqualsOperator = "@NoEqualsMethod class A { public static void method() {A a1 = new A(); A a2 = new A(); System.out.println(a1==a2);} }";
        BugReport reportEqualsOperator = analyser.analyse(codeEqualsOperator);
        Assertions.assertTrue(reportEqualsOperator.getBugs().isEmpty());
    }

    @Test
    public void allowIfOnSameLine() {
        String code = "@NoEqualsMethod class A { public String method(boolean b) { if (b) return \"b is true\"; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void allowIfOnSameLineOnlyOneStatement() {
        String code = "@NoEqualsMethod class A { public void method(boolean b, ArrayList<Integer> lst) { if (b) lst.add(1); lst.add(2); } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
    }

    @Test
    public void arrayEquals() {
        String code = "@NoEqualsMethod class A { public boolean method(int[] a, int[] b) { return a==b; } }";
        Analyser analyser = new Analyser();
        BugReport report = analyser.analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("Arrays.equals(a, b)", error.getSuggestion().get());
        Assertions.assertEquals("A", error.getContainingClass());
    }

    @Test
    public void bitwiseAndOperator() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { if(a==0 & b==0) {} } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof BitwiseOperatorError);
    }

    @Test
    public void bitwiseOrOperator() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { if(a==0 | b==0) {} } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof BitwiseOperatorError);
    }

    @Test
    public void bitwiseOperatorOnNumbers() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { int a = 1 | 2; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void ifWithTwoStatementsWithSameIndentationNotAllowed() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { if (a==b) \n \t System.out.println(\"Hello\"); \n \t System.out.println(\"Hello\"); }}";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof IfWithoutBracketsError);
    }

    @Test
    public void ifWithOneStatementAndSiblingAllowed() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { if (a==b) \n \t System.out.println(\"Hello\"); \n System.out.println(\"Hello\"); }}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void ifWithOneStatementOnSameLineAndSiblingAllowed() {
        String code =
                "@NoEqualsMethod class A { public A(int a, int b) { " +
                "if (a==b) System.out.println(\"Hello\"); \n" +
                "System.out.println(\"Hello\"); }}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void ifStatementSemicolonAllowedAnnotation() {
        String code =
                "@NoEqualsMethod @IfStatementWithSemicolonAllowed class A { public A(int a, int b) { " +
                        "if (a==b); System.out.println(\"Hello\");}}";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void bitwiseOperatorAnnotation() {
        String code = "@NoEqualsMethod @BitwiseOperationAllowed class A { public A(int a, int b) { boolean a = true | false; } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void shouldGiveErrorWhenSiblingOfIfIsOnSameLine() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { if (a==b) a=b; b=a; } }";
        Analyser analyser = new Analyser();
        BugReport report = analyser.analyse(code);
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof IfWithoutBracketsError);
    }

    @Test
    public void ifStatementOnSameLineAnnotation() {
        String code = "@NoEqualsMethod @IfWithoutBracketsAllowed class A { public A(int a, int b) { if (a==b) a=b; b=a; } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void equalsOperatorOnObjectAnnotation() {
        String code = "@NoEqualsMethod @EqualsOperatorOnObjectAllowed class A { public A(Object a, Object b) { boolean bo = a==b; } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void shouldIgnoreErrorWithIntegerDivisionAnnotation() {
        String code = "@NoEqualsMethod @IntegerDivisionAllowed class A { public A(int a, int b) { int bo = a/b; } }";
        BugReport report = new Analyser().analyse(code);
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void shouldIgnoreNoEqualsMethodWhenAbstractClass() {
        String code = "abstract class A {}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

}
