package org.openrewrite.java.migrate.apache.commons.lang;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.tree.*;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public final class DefaultStringRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Refaster template `DefaultString`";
    }

    @Override
    public String getDescription() {
        return "Recipe created for the following Refaster template:\n```java\npublic class DefaultString {\n    \n    @BeforeTemplate\n    String before(String s) {\n        return StringUtils.defaultString(s);\n    }\n    \n    @AfterTemplate\n    String after(String s) {\n        return Objects.toString(s);\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {
            final JavaTemplate before = JavaTemplate.compile(this, "before", (JavaTemplate.F1<?, ?>) (String s) -> StringUtils.defaultString(s)).build();
            final JavaTemplate after = JavaTemplate.compile(this, "after", (JavaTemplate.F1<?, ?>) (String s) -> Objects.toString(s)).build();

            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                maybeRemoveImport("java.lang.String");
                maybeAddImport("java.lang.String");
                maybeRemoveImport("java.util.Objects");
                maybeAddImport("java.util.Objects");
                maybeRemoveImport("org.apache.commons.lang3.StringUtils");
                maybeAddImport("org.apache.commons.lang3.StringUtils");
                JavaTemplate.Matcher matcher;
                if ((matcher = before.matcher(getCursor())).find()) {
                    return after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0));
                }
                return super.visitMethodInvocation(elem, ctx);
            }

        };
    }
}
