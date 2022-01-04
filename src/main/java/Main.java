import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import errors.BugReport;
import visitor.BugFinderVisitor;

public class Main {

    public static void main(String[] args) {
        CompilationUnit compilationUnit = StaticJavaParser.parse("@NoEqualsMethod class A { }");
        BugFinderVisitor visitor = new BugFinderVisitor();
        visitor.visit(compilationUnit, null);
        BugReport report = visitor.getReport();
        System.out.println(report.getBugs());
    }
}
