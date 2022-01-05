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
    public void bitwiseOperator() {
        String code = "@NoEqualsMethod class A { public A(int a, int b) { if(a==0 & b==0) {} } }";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        Assertions.assertFalse(report.getBugs().isEmpty());
    }

}
