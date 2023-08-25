package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.openrewrite.java.template.Matches;
import org.openrewrite.java.template.MethodInvocationMatcher;
import org.openrewrite.java.template.NotMatches;

public class Matching {

    public static class StringIsEmpty {
        @BeforeTemplate
        boolean before(int i, @NotMatches(MethodInvocationMatcher.class) String s) {
            return s.substring(i).isEmpty();
        }

        @BeforeTemplate
        boolean before2(int i, @Matches(MethodInvocationMatcher.class) String s) {
            return s.substring(i).isEmpty();
        }

        @AfterTemplate
        boolean after(String s) {
            return s != null && s.length() == 0;
        }
    }

}
