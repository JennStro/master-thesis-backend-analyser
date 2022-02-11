import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import master.thesis.backend.analyser.Analyser;
import master.thesis.backend.errors.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import master.thesis.backend.visitor.BugFinderVisitor;

import java.io.File;
import java.io.FileNotFoundException;

public class BugFinderTest {

    private BugFinderVisitor visitor = new BugFinderVisitor();

    @BeforeAll
    static void setUp() {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        StaticJavaParser.getConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));
    }

    @Test
    public void noEqualsMethodTest() {
        String code = "class A {}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof MissingEqualsMethodError);
    }

    @Test
    public void noEqualsMethodTestWithAnnotation() {
        String code = "@NoEqualsMethod class A {}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void noEqualsMethodTestWithAnnotationTestClass() {
        String path = "src/test/java/TestClass.java";
        CompilationUnit compilationUnit = null;
        try {
            compilationUnit = StaticJavaParser.parse(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void ifWithBrackets() {
        String code = "@NoEqualsMethod class A { public void method() {if (true) {} }}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void ifWithoutBracketsOneStatementAllowed() {
        String code = "@NoEqualsMethod class A { public void method() {if (true) \n System.out.println(\"\"); }}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void semiAfterIf() {
        String code = "@NoEqualsMethod class A { public void method() {if (true); {System.out.println(\"\");} }}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof SemiColonAfterIfError);
    }

    @Test
    public void noErrorIfStatement() {
        String code = "@NoEqualsMethod class A { public void method() {if (true) {System.out.println(\"\");} }}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void fieldNoDeclaration() {
        String code = "@NoEqualsMethod class A { int a; }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof FieldDeclarationWithoutInitializerError);
    }

    @Test
    public void fieldWithDeclaration() {
        String code = "@NoEqualsMethod class A { int a = 5; }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void fieldWithDeclarationInConstructor() {
        String code = "@NoEqualsMethod class A { int a; public A(int a) {this.a=a;} }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void fieldNoDeclarationIgnored() {
        String code = "@NoEqualsMethod class A { @NoInitialization int a; }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void equalsOperatorOnObject() {
        String code = "@NoEqualsMethod class A { public A(Object a, Object b) { boolean bo = a==b; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof EqualsOperatorError);
    }

    @Test
    public void notEqualsOperatorOnObject() {
        String code = "@NoEqualsMethod class A { public A(Object a, Object b) { boolean bo = a!=b; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof EqualsOperatorError);
    }

    @Test
    public void equalsOperatorOnPrimitive() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { boolean bo = a==b; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void equalsOperatorOnNull() {
        String code = "@NoEqualsMethod class A { public A(Object a) { if (a == null) {} } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void equalsOperatorSuggestion() {
        String code = "@NoEqualsMethod class A { public A(Object a, Object b) { boolean bo = a==b; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());

        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("a.equals(b)", error.getSuggestion().get());
    }

    @Test
    public void notEqualsOperatorSuggestion() {
        String code = "@NoEqualsMethod class A { public A(Object a, Object b) { boolean bo = a!=b; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());

        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("!a.equals(b)", error.getSuggestion().get());
    }

    @Test
    public void uninitializedFieldSuggestion() {
        String code = "@NoEqualsMethod class A { int a; }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());

        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("public A(int a) {\tthis.a = a;}", error.getSuggestion().get());
    }

    @Test
    public void semiAfterIfSuggestion() {
        String code = "@NoEqualsMethod class A { public void method() {if (true); {System.out.println(\"\");} }}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
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
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
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
    public void lineNumber() {
        String code = "@NoEqualsMethod class A { \n" +
                "   @NoEqualsMethod class Bar {  } \n" +
                "   public boolean method() { \n" +
                "       Bar b = new Bar(); \n" +
                "       Bar b2 = new Bar(); \n" +
                "        return b==b2; " +
                "   } " +
                "}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
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
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
    }

    @Test
    public void noInitInnerClass() {
        String path = "src/test/java/SecondTestClass.java";
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
        Assertions.assertEquals("InnerClass", error.getContainingClass());
        Assertions.assertEquals("public InnerClass(int number) {\tthis.number = number;}", error.getSuggestion().get());
        Assertions.assertEquals(9, error.getLineNumber());
    }

    @Test
    public void ignoreEqualsAndInit() {
        String path = "src/test/java/IgnoreEqualsAndInitError.java";
        CompilationUnit compilationUnit = null;
        try {
            compilationUnit = StaticJavaParser.parse(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
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
        Assertions.assertEquals("to add the method @Override public boolean equals(Object o) { \\Checks to decide if two objects are equal goes here }", error.getSuggestion().get());
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
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("EqualsOperatorClass", error.getContainingClass());
        Assertions.assertEquals("i.equals(i2)", error.getSuggestion().get());
    }

    @Test
    public void fieldInitClass() {
        String path = "src/test/java/FieldInitClass.java";
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
        Assertions.assertEquals("FieldInitClass", error.getContainingClass());
        Assertions.assertEquals("public FieldInitClass(ArrayList<String> list) {\tthis.list = list;}", error.getSuggestion().get());
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
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
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
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
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
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
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
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void bitwiseOperatorAnnotation() {
        String code = "@NoEqualsMethod @BitwiseOperationAllowed class A { public A(int a, int b) { boolean a = true | false; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void ifStatementOnSameLine() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { if (a==b) a=b; b=a; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof IfWithoutBracketsError);
    }

}
