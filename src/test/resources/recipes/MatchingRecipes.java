package foo;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.tree.*;

import java.util.Arrays;
import java.util.List;

import org.openrewrite.java.template.MethodInvocationMatcher;

public final class MatchingRecipes extends Recipe {

    @Override
    public String getDisplayName() {
        return "Refaster recipes for `foo.Matching`";
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
            return "Recipe created for the following Refaster template:\n```java\npublic static class StringIsEmpty {\n    \n    @BeforeTemplate()\n    boolean before(@NotMatches(value = MethodInvocationMatcher.class)\n    String s) {\n        return s.isEmpty();\n    }\n    \n    @AfterTemplate()\n    boolean after(String s) {\n        return s != null && s.length() == 0;\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            return new JavaVisitor<ExecutionContext>() {
                final JavaTemplate before = JavaTemplate.compile(this, "before", (JavaTemplate.F1<?, ?>) (String s) -> s.isEmpty()).build();
                final JavaTemplate after = JavaTemplate.compile(this, "after", (String s) -> s != null && s.length() == 0).build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (new MethodInvocationMatcher().matches((Expression) matcher.parameter(0))) {
                            return super.visitMethodInvocation(elem, ctx);
                        }
                        maybeRemoveImport("org.openrewrite.java.template.MethodInvocationMatcher");
                        doAfterVisit(new org.openrewrite.java.ShortenFullyQualifiedTypeReferences().getVisitor());
                        doAfterVisit(new org.openrewrite.java.cleanup.UnnecessaryParenthesesVisitor());
                        return after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(0));
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
        }
    }

}
