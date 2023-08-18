package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

public class ShouldAddImports {

    public static class StringValueOf {
        @BeforeTemplate
        String before(String s) {
            return String.valueOf(s);
        }

        @AfterTemplate
        String after(String s) {
            return java.util.Objects.toString(s);
        }
    }

    public static class ObjectsEquals {
        @BeforeTemplate
        boolean equals(int a, int b) {
            return java.util.Objects.equals(a, b);
        }
        @BeforeTemplate
        boolean compareZero(int a, int b) {
            return Integer.compare(a, b) == 0;
        }

        @AfterTemplate
        boolean isis(int a, int b) {
            return a == b;
        }
    }
}
