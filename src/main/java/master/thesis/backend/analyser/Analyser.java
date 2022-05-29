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

    private AnalyserConfiguration configuration;

    /**
     * Analyses the given code as string using {@link BugFinderVisitor}.
     *
     * If no {@link AnalyserConfiguration} is given, {@link AnnotationsAdapter} will be used to ignore errors.
     * Only the types within the code to analyse will be resolved.
     * If a type is not resolved, an exception will be added to {@link BugReport}.
     * If the code has a parseerror, an exception will be added to {@link BugReport}.
     *
     * @param code to analyse as string
     * @return the report of this analysis.
     */
    public BugReport analyse(String code) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        StaticJavaParser.getConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));

        try {
            CompilationUnit compilationUnit = StaticJavaParser.parse(code);
            if (configuration == null) {
                configuration = new AnnotationsAdapter(compilationUnit);
            }
            BugFinderVisitor visitor = new BugFinderVisitor(configuration);
            visitor.visit(compilationUnit, null);
            return visitor.getReport();
        } catch (Throwable e) {
            BugReport report = new BugReport();
            report.attach(e);
            return report;
        }
    }

    /**
     *
     * Optional configuration, needs to implement {@link AnalyserConfiguration}.
     * If no configuration is set, {@link AnnotationsAdapter} will be used.
     *
     * @param configuration the configuration to be set
     */
    public void setConfiguration(AnalyserConfiguration configuration) {
        this.configuration = configuration;
    }

}
