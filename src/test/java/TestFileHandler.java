import filewriter.FileHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestFileHandler {

    FileHandler fileHandler = new FileHandler();

    @Test
    public void shouldReturnClassNameOfFile() {
        Assertions.assertEquals("B", fileHandler.getClassName("class B {}"));
    }

    @Test
    public void shouldAddPackageDeclarationToClass() {
        Assertions.assertEquals("package dependenciesForAnalysis; class B {} ", fileHandler.addPackageDeclaration("class B {}"));
    }

    @Test
    public void shouldReplacePackageDeclaration() {
        Assertions.assertEquals("package dependenciesForAnalysis; class B {} ",
                fileHandler.replacePackageDeclaration("package myPackage; class B {}"));
    }
}
