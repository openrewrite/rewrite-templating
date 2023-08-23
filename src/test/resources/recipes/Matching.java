package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.openrewrite.java.template.Matches;
import org.openrewrite.java.template.MethodInvocationMatcher;
import org.openrewrite.java.template.NotMatches;

public class Matching {

    public static class StringIsEmpty {
        @BeforeTemplate
        boolean before(@NotMatches(MethodInvocationMatcher.class) String s) {
            return s.isEmpty();
        }

        @BeforeTemplate
        boolean before2(@Matches(MethodInvocationMatcher.class) String s) {
            return s.isEmpty();
        }

        @AfterTemplate
        boolean after(String s) {
            return s != null && s.length() == 0;
        }
    }

}
