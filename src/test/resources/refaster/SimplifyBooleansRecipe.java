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
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption;
import org.openrewrite.java.tree.J;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipe created for Refaster template {@code SimplifyBooleans}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class SimplifyBooleansRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public SimplifyBooleansRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `SimplifyBooleans`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\npublic class SimplifyBooleans {\n    \n    @BeforeTemplate\n    String before(String s, String s1, String s2) {\n        return s.replaceAll(s1, s2);\n    }\n    \n    @AfterTemplate\n    String after(String s, String s1, String s2) {\n        return s != null ? s.replaceAll(s1, s2) : s;\n    }\n}\n```\n.";
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
                    before = JavaTemplate.builder("#{s:any(java.lang.String)}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)})")
                            .bindType("java.lang.String").build();
                }
                if ((matcher = before.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("#{s:any(java.lang.String)} != null ? #{s}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)}) : #{s}")
                                .bindType("java.lang.String").build();
                    }
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
                new UsesMethod<>("java.lang.String replaceAll(..)", true),
                javaVisitor
        );
    }
}
