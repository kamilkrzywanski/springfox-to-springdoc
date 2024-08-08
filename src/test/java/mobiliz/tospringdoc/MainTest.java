package mobiliz.tospringdoc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErr;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;



public class MainTest {

    @Test
    public void printsHelp() throws Exception {
        String out = tapSystemOut(() -> {
            Main.main("-h");
        });
        String expected = "usage: java -jar stringfox-to-springdoc.jar <src> [options]\n" +
            " -h,--help        display this help and exit\n" +
            " -i,--in-place    migrate files in place\n" +
            " -o,--out <arg>   write migrated source instead of stdout. This option is\n" +
            "                  discarded if in-place option set.\n";
        Assertions.assertEquals(expected, out);
    }

    @Test
    public void printErrIfSourceNotSpecified() throws Exception {
        String err = tapSystemErr(() -> {
            Main.main();
        });
        Assertions.assertEquals("ERROR: source is not specified.\n", err);
    }
}
