package master.thesis.backend.analyser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import master.thesis.backend.errors.BugReport;
import master.thesis.backend.visitor.BugFinderVisitor;

public class Analyser {

    public BugReport analyse(String code) {
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        BugFinderVisitor visitor = new BugFinderVisitor();
        visitor.visit(compilationUnit, null);
        return visitor.getReport();
    }
}
