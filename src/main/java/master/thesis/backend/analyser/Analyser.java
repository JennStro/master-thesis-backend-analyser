package master.thesis.backend.analyser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import master.thesis.backend.adapter.AnnotationsAdapter;
import master.thesis.backend.errors.BugReport;
import master.thesis.backend.visitor.BugFinderVisitor;

public class Analyser {

    public BugReport analyse(String code) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        StaticJavaParser.getConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));

        try {
            CompilationUnit compilationUnit = StaticJavaParser.parse(code);
            AnalyserConfiguration adapter = new AnnotationsAdapter(compilationUnit);
            BugFinderVisitor visitor = new BugFinderVisitor(adapter);
            visitor.visit(compilationUnit, null);
            return visitor.getReport();
        } catch (Throwable e) {
            BugReport report = new BugReport();
            report.attach(e);
            return report;
        }
    }
}
