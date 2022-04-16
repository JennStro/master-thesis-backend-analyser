import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import master.thesis.backend.visitor.AnnotationVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAnnotationVisitor {

    private AnnotationVisitor visitor = new AnnotationVisitor();

    @Test
    public void shouldHaveTwoAnnotations() {
        String code =
                "@Something " +
                "@SomethingElse " +
                "class A {}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        Assertions.assertEquals(2, visitor.getAnnotations().size());
    }

    @Test
    public void shouldHaveAnnotationOnlyInnerClass() {
        String code =
                "@Something " +
                "@SomethingElse " +
                "class A {" +
                "   @OnlyForClassB" +
                "   class B {}" +
                "}";
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        visitor.visit(compilationUnit, null);
        Assertions.assertEquals(1, visitor.getAnnotationsForClass("B").size());
        Assertions.assertEquals("@OnlyForClassB",visitor.getAnnotationsForClass("B").get(0));
    }

}
