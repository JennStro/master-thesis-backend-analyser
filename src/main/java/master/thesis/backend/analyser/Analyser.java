package master.thesis.backend.analyser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import filewriter.FileHandler;
import master.thesis.backend.adapter.AnnotationsAdapter;
import master.thesis.backend.errors.BugReport;
import master.thesis.backend.visitor.BugFinderVisitor;

public class Analyser {

    private AnalyserConfiguration configuration;

    public final static String PATH_FOR_DEPENDENCIES = "src/main/resources";
    public final static String FILE_PATH_FOR_DEPENDENCIES = PATH_FOR_DEPENDENCIES + "/";
    private final FileHandler dependencyHandler = new FileHandler();

    public BugReport analyse(String code) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        TypeSolver context = new JavaParserTypeSolver(PATH_FOR_DEPENDENCIES);
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(context);
        StaticJavaParser.getConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));

        try {
            CompilationUnit compilationUnit = StaticJavaParser.parse(code);
            if (configuration == null) {
                configuration = new AnnotationsAdapter(compilationUnit);
            }
            BugFinderVisitor visitor = new BugFinderVisitor(configuration);
            visitor.visit(compilationUnit, null);
            dependencyHandler.deleteFiles();
            return visitor.getReport();
        } catch (Throwable e) {
            BugReport report = new BugReport();
            report.attach(e);
            dependencyHandler.deleteFiles();
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

    /**
     *
     * @param dependency Java code as string
     */
    public void addDependency(String dependency) {
        dependencyHandler.createNewFileAndWrite(dependency);
    }
}
