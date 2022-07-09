package org.openrewrite.sample;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.template.After;
import org.openrewrite.java.template.Before;
import org.openrewrite.java.template.Template;
import org.openrewrite.java.template.TemplateMatcher;

public class StringIsEmpty extends Recipe {
    @Override
    public String getDisplayName() {
        return "Use `String#isEmpty()` rather than comparing length";
    }

    @Override
    public String getDescription() {
        return "This method is purpose built for this case, and is easier to read.";
    }

    @Override
    protected JavaVisitor<ExecutionContext> getVisitor() {
        return new TemplateMatcher(new Template() {
            @Before
            boolean equalsEmptyString(String s) {
                return s.equals("");
            }

            @Before
            boolean lengthEquals0(String s) {
                return s.length() == 0;
            }

            @After
            boolean optimizedMethod(String s) {
                return s.isEmpty();
            }
        }).replaceAll();
    }
}
