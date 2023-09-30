package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.openrewrite.java.template.Matches;
import org.openrewrite.java.template.MethodInvocationMatcher;
import org.openrewrite.java.template.NotMatches;

public class ParameterReuse {
    @BeforeTemplate
    boolean before(String s) {
        return s.equals(s);
    }

    @AfterTemplate
    boolean after() {
        return true;
    }
}
