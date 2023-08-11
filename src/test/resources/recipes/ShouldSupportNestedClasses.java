package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

public class ShouldSupportNestedClasses {
    public static class NestedClass {
        @BeforeTemplate
        boolean before(String s) {
            return s.length() > 0;
        }

        @AfterTemplate
        boolean after(String s) {
            return !s.isEmpty();
        }
    }
}