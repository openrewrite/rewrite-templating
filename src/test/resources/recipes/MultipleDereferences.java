package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

public class MultipleDereferences {

    public static class StringIsEmpty {
        @BeforeTemplate
        boolean before(String s) {
            return s.isEmpty();
        }

        @AfterTemplate
        boolean after(String s) {
            return s != null && s.length() == 0;
        }
    }

    public static class EqualsItself {
        @BeforeTemplate
        boolean before(Object o) {
            return o == o;
        }

        @AfterTemplate
        boolean after(Object o) {
            return true;
        }
    }

}
