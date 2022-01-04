import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import errors.BugReport;
import errors.MissingEqualsMethodError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import visitor.BugFinderVisitor;

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

}
