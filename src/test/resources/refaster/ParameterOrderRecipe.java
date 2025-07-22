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
import org.openrewrite.java.tree.J;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipe created for Refaster template {@code ParameterOrder}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class ParameterOrderRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public ParameterOrderRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `ParameterOrder`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\npublic class ParameterOrder {\n    \n    @BeforeTemplate\n    public int parameters(int b, int a) {\n        return a + b;\n    }\n    \n    @AfterTemplate\n    public int output(int a, int b) {\n        return a + a + b;\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractRefasterJavaVisitor() {
            JavaTemplate parameters;
            JavaTemplate after;

            @Override
            public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (parameters == null) {
                    parameters = JavaTemplate.builder("#{a:any(int)} + #{b:any(int)}").build();
                }
                if ((matcher = parameters.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("#{a:any(int)} + #{a} + #{b:any(int)}").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                return super.visitBinary(elem, ctx);
            }

        };
    }
}
