package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MethodThrows {
    @BeforeTemplate
    void before(Path path) throws IOException {
        Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    @AfterTemplate
    void after(Path path) throws Exception {
        Files.readAllLines(path);
    }
}
