package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

import java.util.Objects;

public class ShouldAddImports {

    public static class StringValueOf {
        @BeforeTemplate
        String before(String s) {
            return String.valueOf(s);
        }

        @AfterTemplate
        String after(String s) {
            return Objects.toString(s);
        }
    }

    public static class ObjectsEquals {
        @BeforeTemplate
        boolean equals(int a, int b) {
            return Objects.equals(a, b);
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
