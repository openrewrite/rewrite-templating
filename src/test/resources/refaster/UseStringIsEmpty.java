package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

public class UseStringIsEmpty {
    @BeforeTemplate
    boolean before(String s) {
        return s.length() > 0;
    }

    @AfterTemplate
    boolean after(String s) {
        return !s.isEmpty();
    }
}
