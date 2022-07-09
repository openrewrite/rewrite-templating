package org.openrewrite.java.template;

import org.openrewrite.Cursor;
import org.openrewrite.Recipe;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.template.internal.JavaTemplateGenerator;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

public class TemplateMatcher {
    private final JavaTemplateGenerator javaTemplateGenerator;

    /**
     * Uses reflection to look up a generated class for this template containing
     * the {@link JavaTemplate} code that we are going to execute.
     *
     * @param template The template.
     */
    public TemplateMatcher(Template template) {
        // TODO look up through reflection
        this.javaTemplateGenerator = null;
    }

    public <J2 extends J> J2 replace(Cursor cursor) {
        Statement statement = cursor.getValue();
        return statement.withTemplate(javaTemplateGenerator.build(cursor), statement.getCoordinates().replace(),
                javaTemplateGenerator.getParameters(cursor));
    }

    public <P> JavaVisitor<P> replaceAll() {
        return new JavaVisitor<>() {
            @Override
            public @Nullable J preVisit(J tree, P p) {
                return super.preVisit(tree, p);
            }
        };
    }
}
