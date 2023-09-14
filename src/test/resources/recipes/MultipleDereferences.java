package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MultipleDereferences {

    public static class VoidType {
        @BeforeTemplate
        void before(Path p) throws IOException {
            Files.delete(p);
        }

        @AfterTemplate
        void after(Path p)throws IOException {
            Files.delete(p);
        }
    }

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
