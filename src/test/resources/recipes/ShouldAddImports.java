package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

import java.util.Objects;

public class ShouldAddImports {
    @BeforeTemplate
    String before(String s) {
        return String.valueOf(s);
    }

    @AfterTemplate
    String after(String s) {
        return Objects.toString(s);
    }
}
