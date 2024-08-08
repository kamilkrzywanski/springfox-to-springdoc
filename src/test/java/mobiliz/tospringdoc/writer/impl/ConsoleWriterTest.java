package mobiliz.tospringdoc.writer.impl;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;


import mobiliz.tospringdoc.core.MigrationUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConsoleWriterTest extends AbstractSourceWriterTest {

    @Test
    public void printsSourceToStdoutWithPath() throws Exception {
        loadSource();
        ConsoleWriter consoleWriter = new ConsoleWriter();
        String out = tapSystemOut(() -> {
            consoleWriter.write(new MigrationUnit("/tmp/TestController.java", compilationUnit));
        });
        String expected = "/tmp/TestController.java:\n" + compilationUnit.toString();
        Assertions.assertEquals(expected, out);
    }

    @Test
    public void printsSourceToStdoutWithoutPath() throws Exception {
        loadSource();
        ConsoleWriter consoleWriter = new ConsoleWriter();
        consoleWriter.setTitled(false);
        String out = tapSystemOut(() -> {
            consoleWriter.write(new MigrationUnit("/tmp/TestController.java", compilationUnit));
        });
        Assertions.assertEquals(compilationUnit.toString(), out);
    }
}
