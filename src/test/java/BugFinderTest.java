import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
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
    public void ifWithoutBrackets() {
        String code = "@NoEqualsMethod class A { public void method() {if (true) System.out.println(\"\"); }}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof IfWithoutBracketsError);
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
        String code = "@NoEqualsMethod class A { public A(Object a, Object b) { System.out.println(a==b); } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof EqualsOperatorError);
    }

    @Test
    public void notEqualsOperatorOnObject() {
        String code = "@NoEqualsMethod class A { public A(Object a, Object b) { System.out.println(a!=b); } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof EqualsOperatorError);
    }

    @Test
    public void equalsOperatorOnPrimitive() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { System.out.println(a==b); } }";
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
    public void ignoringReturnError() {
        String code = "@NoEqualsMethod class A { public String method(String a) { a.toLowerCase(); return a; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        Assertions.assertTrue(report.getBugs().get(0) instanceof IgnoringReturnError);
    }

    @Test
    public void notIgnoringReturnError() {
        String code = "@NoEqualsMethod class A { public String method(String a) { return a.toLowerCase(); } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void ignoringReturnSuggestion() {
        String code = "@NoEqualsMethod class A { public String method(String a) { a.toLowerCase(); return a;} }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());

        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("You should try \n" +
                "\n" +
                "java.lang.String variableName = a.toLowerCase();", error.getSuggestion().get());
    }

    @Test
    public void equalsOperatorSuggestion() {
        String code = "@NoEqualsMethod class A { public A(Object a, Object b) { System.out.println(a==b); } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());

        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("You should try a.equals(b)", error.getSuggestion().get());
    }

    @Test
    public void notEqualsOperatorSuggestion() {
        String code = "@NoEqualsMethod class A { public A(Object a, Object b) { System.out.println(a!=b); } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());

        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("You should try !a.equals(b)", error.getSuggestion().get());
    }

    @Test
    public void bitwiseOperatorSuggestion() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { if(a==0 | b==0) {} } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());

        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("You should try: \n" +
                " \n" +
                "a == 0 || b == 0", error.getSuggestion().get());
    }

    @Test
    public void bitwiseAndOperatorSuggestion() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { if(a==0 & b==0) {} } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());

        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("You should try: \n" +
                " \n" +
                "a == 0 && b == 0", error.getSuggestion().get());
    }

    @Test
    public void uninitializedFieldSuggestion() {
        String code = "@NoEqualsMethod class A { int a; }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());

        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("You could initialize the fieldvariable in the constructor: \n" +
                " \n" +
                "public A(int a) { \n" +
                " \tthis.a = a;\n" +
                "}", error.getSuggestion().get());
    }

    @Test
    public void semiAfterIfSuggestion() {
        String code = "@NoEqualsMethod class A { public void method() {if (true); {System.out.println(\"\");} }}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());

        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("You should try \n" +
                " \n" +
                " if (true) {\n" +
                " \t// ...your code here... \n" +
                "}", error.getSuggestion().get());
    }

    @Test
    public void ifWithoutBlockSuggestion() {
        String code = "@NoEqualsMethod class A { public void method() {if (true) System.out.println(\"\"); }}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());

        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("You should enclose the body in brackets: \n" +
                "if (true) { \n" +
                "    System.out.println(\"\");\n" +
                "}", error.getSuggestion().get());
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
    public void unresolvedMethodCallExceptionWithError() {
        String code = "@NoEqualsMethod class A { public String method() { Bar b = new Bar(); b.toString(); return b; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void resolvedMethodCallExceptionWithError() {
        String code = "@NoEqualsMethod class A { @NoEqualsMethod class Bar {  } public String method() { Bar b = new Bar(); b.toString(); return b; } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("You should try \n" +
                "\n" +
                "java.lang.String variableName = b.toString();", error.getSuggestion().get());
    }

    @Test
    public void unresolvedVariableExceptionEqualsOperator() {
        String code = "@NoEqualsMethod class A { public String method(Bar b, Bar b1) { if (b==b1) {return b.toString();} } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("You should try b.equals(b1)", error.getSuggestion().get());
    }

    @Test
    public void unresolvedVariableExceptionEqualsOperatorNoError() {
        String code = "@NoEqualsMethod class A { public String method(Bar b, Bar b1) { if (b.getNumber() ==b1.getNumber()) {return b.toString();} } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertTrue(report.getBugs().isEmpty());
    }

    @Test
    public void lineNumber() {
        String code = "@NoEqualsMethod class A { \n" +
                "   @NoEqualsMethod class Bar {  } \n" +
                "   public String method() { \n" +
                "       Bar b = new Bar(); \n" +
                "       b.toString(); \n" +
                "       return b; " +
                "   } " +
                "}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals(5, error.getLineNumber());
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
        BaseError error = report.getBugs().get(0);
        Assertions.assertEquals("InnerClass", error.getContainingClass());
        Assertions.assertEquals("You should add the method \n" +
                " \n" +
                " @Override \n" +
                "public boolean equals(Object o) { \n" +
                "   //... Your implementation here... \n" +
                "}", error.getSuggestion().get());
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
        Assertions.assertEquals("You could initialize the fieldvariable in the constructor: \n" +
                " \n" +
                "public InnerClass(int number) { \n" +
                " \tthis.number = number;\n" +
                "}", error.getSuggestion().get());
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
        Assertions.assertEquals("You should add the method \n" +
                " \n" +
                " @Override \n" +
                "public boolean equals(Object o) { \n" +
                "   //... Your implementation here... \n" +
                "}", error.getSuggestion().get());
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
        Assertions.assertEquals("You should try i.equals(i2)", error.getSuggestion().get());
    }

    @Test
    public void bitwiseOperatorClass() {
        String path = "src/test/java/BitwiseOperatorClass.java";
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
        Assertions.assertEquals("BitwiseOperatorClass", error.getContainingClass());
        Assertions.assertEquals("You should try: \n" +
                " \n" +
                "bar.drinksHaveBeenInitialized() && bar.barHasMoreThenFiveDrinks()", error.getSuggestion().get());
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
        Assertions.assertEquals("You could initialize the fieldvariable in the constructor: \n" +
                " \n" +
                "public FieldInitClass(ArrayList<String> list) { \n" +
                " \tthis.list = list;\n" +
                "}", error.getSuggestion().get());
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
        Assertions.assertEquals("You should enclose the body in brackets: \n" +
                "if (shouldAddToList) { \n" +
                "    list.add(\"1\");\n" +
                "}", error.getSuggestion().get());
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

}
