package foo;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.internal.template.AbstractRefasterJavaVisitor;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.tree.*;

import java.util.function.Supplier;

import java.util.Arrays;
import java.util.List;


public final class MatchingRecipes extends Recipe {

    @Override
    public String getDisplayName() {
        return "`Matching` Refaster recipes";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.Matching`.";
    }


    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new StringIsEmptyRecipe()
        );
    }
    public static class StringIsEmptyRecipe extends Recipe {

        @Override
        public String getDisplayName() {
            return "Refaster template `Matching.StringIsEmpty`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class StringIsEmpty {\n    \n    @BeforeTemplate()\n    boolean before(int i, @NotMatches(value = MethodInvocationMatcher.class)\n    String s) {\n        return s.substring(i).isEmpty();\n    }\n    \n    @BeforeTemplate()\n    boolean before2(int i, @Matches(value = MethodInvocationMatcher.class)\n    String s) {\n        return s.substring(i).isEmpty();\n    }\n    \n    @AfterTemplate()\n    boolean after(String s) {\n        return s != null && s.length() == 0;\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {

                Supplier<JavaTemplate> before = memoize(() -> JavaTemplate.compile(this, "before", (JavaTemplate.F2<?, ?, ?>) (@Primitive Integer i, String s) -> s.substring(i).isEmpty()).build());

                Supplier<JavaTemplate> before2 = memoize(() -> JavaTemplate.compile(this, "before2", (JavaTemplate.F2<?, ?, ?>) (@Primitive Integer i, String s) -> s.substring(i).isEmpty()).build());

                Supplier<JavaTemplate> after = memoize(() -> JavaTemplate.compile(this, "after", (String s) -> s != null && s.length() == 0).build());

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = matcher(before, getCursor())).find()) {
                        if (new org.openrewrite.java.template.MethodInvocationMatcher().matches((Expression) matcher.parameter(1))) {
                            return super.visitMethodInvocation(elem, ctx);
                        }
                        return embed(
                                apply(after, getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(0)),
                                getCursor(),
                                ctx
                        );
                    }
                    if ((matcher = matcher(before2, getCursor())).find()) {
                        if (!new org.openrewrite.java.template.MethodInvocationMatcher().matches((Expression) matcher.parameter(1))) {
                            return super.visitMethodInvocation(elem, ctx);
                        }
                        return embed(
                                apply(after, getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(0)),
                                getCursor(),
                                ctx
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(new UsesMethod<>("java.lang.String isEmpty(..)"), new UsesMethod<>("java.lang.String substring(..)")),
                    javaVisitor);
        }
    }

}
