package mobiliz.tospringdoc.migrator.impl;

import io.swagger.annotations.Authorization;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AuthorizationTest extends AbstractSampleTest {

    public AuthorizationTest() {
        super(Authorization.class);
    }

    @Test
    public void simple() throws IOException {
        testBySample("simple");
    }

}
