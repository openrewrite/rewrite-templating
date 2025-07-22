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
 * OpenRewrite recipe created for Refaster template {@code OrElseGetGet}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class OrElseGetGetRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public OrElseGetGetRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `OrElseGetGet`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\nclass OrElseGetGet<T> {\n    \n    @BeforeTemplate\n    T before(Optional<T> o1, Optional<T> o2) {\n        return o1.orElseGet(()->o2.get());\n    }\n    \n    @AfterTemplate\n    T after(Optional<T> o1, Optional<T> o2) {\n        return o1.orElseGet(o2::get);\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            JavaTemplate before;
            JavaTemplate after;

            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (before == null) {
                    before = JavaTemplate.builder("#{o1:any(java.util.Optional<T>)}.orElseGet(()->#{o2:any(java.util.Optional<T>)}.get())")
                            .bindType("T")
                            .genericTypes("T").build();
                }
                if ((matcher = before.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("#{o1:any(java.util.Optional<T>)}.orElseGet(#{o2:any(java.util.Optional<T>)}::get)")
                                .bindType("T")
                                .genericTypes("T").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                return super.visitMethodInvocation(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("java.util.Optional", true),
                        new UsesMethod<>("java.util.Optional get(..)", true),
                        new UsesMethod<>("java.util.Optional orElseGet(..)", true)
                ),
                javaVisitor
        );
    }
}
