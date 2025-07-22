package foo;

import java.util.*;
import javax.annotation.Generated;
import org.jspecify.annotations.NullMarked;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption;
import org.openrewrite.java.tree.J;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipe created for Refaster template {@code StringIsEmptyPredicate}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class StringIsEmptyPredicateRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public StringIsEmptyPredicateRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `StringIsEmptyPredicate`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\nclass StringIsEmptyPredicate {\n    \n    @BeforeTemplate\n    Predicate<String> before() {\n        return (s)->s.isEmpty();\n    }\n    \n    @AfterTemplate\n    Predicate<String> after() {\n        return String::isEmpty;\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            JavaTemplate before;
            JavaTemplate after;

            @Override
            public J visitLambda(J.Lambda elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (before == null) {
                    before = JavaTemplate.builder("(s)->s.isEmpty()")
                            .bindType("java.util.function.Predicate<java.lang.String>").build();
                }
                if ((matcher = before.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("String::isEmpty")
                                .bindType("java.util.function.Predicate<java.lang.String>").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace()),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                return super.visitLambda(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("java.util.function.Predicate", true),
                        new UsesMethod<>("java.lang.String isEmpty(..)", true)
                ),
                javaVisitor
        );
    }
}
