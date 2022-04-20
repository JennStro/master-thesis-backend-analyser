import filewriter.FileHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestFileHandler {

    FileHandler fileHandler = new FileHandler();

    @Test
    public void shouldReturnClassNameOfFile() {
        Assertions.assertEquals("B", fileHandler.getClassName("class B {}"));
    }
}
