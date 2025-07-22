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
 * OpenRewrite recipes created for Refaster template {@code foo.Lambdas}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class LambdasRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public LambdasRecipes() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "`Lambdas` Refaster recipes";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Refaster template recipes for `foo.Lambdas`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new UsedLambdaRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code Lambdas.UsedLambda}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class UsedLambdaRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public UsedLambdaRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `Lambdas.UsedLambda`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class UsedLambda {\n    \n    @BeforeTemplate\n    void before(List<Integer> is) {\n        is.sort((x,y)->x - y);\n    }\n    \n    @AfterTemplate\n    void after(List<Integer> is) {\n        is.sort(Comparator.comparingInt((x)->x));\n    }\n}\n```\n.";
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
                        before = JavaTemplate.builder("#{is:any(java.util.List<java.lang.Integer>)}.sort((x,y)->x - y);").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{is:any(java.util.List<java.lang.Integer>)}.sort(java.util.Comparator.comparingInt((x)->x));").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
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
                            new UsesType<>("java.util.List", true),
                            new UsesMethod<>("java.util.List sort(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

}
