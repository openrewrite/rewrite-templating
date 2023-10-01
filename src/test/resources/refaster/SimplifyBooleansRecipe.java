package foo;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.NonNullApi;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.template.Semantics;
import org.openrewrite.java.template.function.*;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.tree.*;

import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;


@NonNullApi
public class SimplifyBooleansRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Refaster template `SimplifyBooleans`";
    }

    @Override
    public String getDescription() {
        return "Recipe created for the following Refaster template:\n```java\npublic class SimplifyBooleans {\n    \n    @BeforeTemplate()\n    String before(String s, String s1, String s2) {\n        return s.replaceAll(s1, s2);\n    }\n    \n    @AfterTemplate()\n    String after(String s, String s1, String s2) {\n        return s != null ? s.replaceAll(s1, s2) : s;\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            final JavaTemplate before = Semantics.expression(this, "before", (String s, String s1, String s2) -> s.replaceAll(s1, s2)).build();
            final JavaTemplate after = Semantics.expression(this, "after", (String s, String s1, String s2) -> s != null ? s.replaceAll(s1, s2) : s).build();

            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = before.matcher(getCursor())).find()) {
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1), matcher.parameter(2)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                return super.visitMethodInvocation(elem, ctx);
            }

        };
        return Preconditions.check(
                new UsesMethod<>("java.lang.String replaceAll(..)"),
                javaVisitor
        );
    }
}
