package foo;

import java.util.*;
import javax.annotation.Generated;
import org.jspecify.annotations.NullMarked;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipe created for Refaster template {@code AnnotatedUnusedArgument}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class AnnotatedUnusedArgumentRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public AnnotatedUnusedArgumentRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `AnnotatedUnusedArgument`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\npublic class AnnotatedUnusedArgument {\n    \n    @BeforeTemplate\n    public int before1(int a, @Matches(value = MethodInvocationMatcher.class)\n    int b) {\n        return a;\n    }\n    \n    @BeforeTemplate\n    public int before2(int a, @NotMatches(value = MethodInvocationMatcher.class)\n    int c) {\n        return a;\n    }\n    \n    @AfterTemplate\n    public int after(int a) {\n        return a;\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractRefasterJavaVisitor() {
            JavaTemplate before1;
            JavaTemplate before2;
            JavaTemplate after;

            @Override
            public J visitExpression(Expression elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (before1 == null) {
                    before1 = JavaTemplate.builder("#{a:any(int)}").build();
                }
                if ((matcher = before1.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("#{a:any(int)}").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                if (before2 == null) {
                    before2 = JavaTemplate.builder("#{a:any(int)}").build();
                }
                if ((matcher = before2.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("#{a:any(int)}").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                return super.visitExpression(elem, ctx);
            }

        };
    }
}
