import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import visitor.BugFinderVisitor;

public class Main {

    public static void main(String[] args) {
        CompilationUnit compilationUnit = StaticJavaParser.parse("class A {}");
        BugFinderVisitor visitor = new BugFinderVisitor();
        visitor.visit(compilationUnit, null);
    }
}
