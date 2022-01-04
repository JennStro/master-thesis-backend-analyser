import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import master.thesis.backend.errors.BugReport;
import master.thesis.backend.errors.IfWithoutBracketsError;
import master.thesis.backend.errors.MissingEqualsMethodError;
import master.thesis.backend.errors.SemiColonAfterIfError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import master.thesis.backend.visitor.BugFinderVisitor;

import java.io.File;
import java.io.FileNotFoundException;

public class BugFinderTest {

    private BugFinderVisitor visitor = new BugFinderVisitor();

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

}
