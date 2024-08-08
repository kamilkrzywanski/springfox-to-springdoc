package mobiliz.tospringdoc.util;



import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilsTest {

    @Test
    public void getsJavaFiles() throws IOException {
        List<File> javaFiles = FileUtils.getJavaFiles("./src/main/java/mobiliz/tospringdoc/util");
        assertNotNull(javaFiles);
        assertEquals(javaFiles.size(), 3);
        Set<String> names = javaFiles.stream().map(File::getName).collect(Collectors.toSet());
        assertTrue(names.contains("FileUtils.java"));
        assertTrue(names.contains("NodeUtils.java"));
        assertTrue(names.contains("ResponseUtils.java"));
    }
}
